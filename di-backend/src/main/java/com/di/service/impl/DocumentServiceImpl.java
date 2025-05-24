package com.di.service.impl;

import com.di.dto.DocumentDTO;
import com.di.dto.DocumentProcessingMessage;
import com.di.exception.FileProcessingException;
import com.di.exception.ResourceNotFoundException;
import com.di.model.Document;
import com.di.model.DocumentType;
import com.di.model.ProcessingStatus;
import com.di.model.User;
import com.di.model.elasticsearch.DocumentIndex;
import com.di.repository.DocumentRepository;
import com.di.repository.UserRepository;
import com.di.repository.elasticsearch.DocumentIndexRepository;
import com.di.service.DocumentService;
import com.di.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final KafkaTemplate<String, DocumentProcessingMessage> kafkaTemplate;
    private final DocumentIndexRepository documentIndexRepository;

    @Value("${application.kafka.topics.document-upload}")
    private String documentUploadTopic;

    @Override
    @Transactional
    public DocumentDTO uploadDocument(MultipartFile file, String title, String author, String username) {
        try {
            // Get user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

            // Determine document type
            DocumentType documentType = determineDocumentType(file.getOriginalFilename());

            // Create document entity with PENDING status
            Document document = Document.builder()
                    .title(title)
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .author(author)
                    .uploadDate(LocalDateTime.now())
                    .uploadedBy(user)
                    .documentType(documentType)
                    .processingStatus(ProcessingStatus.PENDING)
                    .build();

            // Save document metadata to get an ID
            Document savedDocument = documentRepository.save(document);

            // Store the file in the file system
            String filePath = fileStorageService.storeFile(file, savedDocument.getId());

            // Update the document with the file path
            savedDocument.setFilePath(filePath);
            savedDocument = documentRepository.save(savedDocument);

            // Create a message for Kafka
            DocumentProcessingMessage message = DocumentProcessingMessage.builder()
                    .documentId(savedDocument.getId())
                    .fileName(savedDocument.getFileName())
                    .contentType(savedDocument.getContentType())
                    .filePath(savedDocument.getFilePath())
                    .title(savedDocument.getTitle())
                    .author(savedDocument.getAuthor())
                    .uploadedBy(savedDocument.getUploadedBy().getUsername())
                    .uploadDate(savedDocument.getUploadDate())
                    .build();

            // Send the message to Kafka for asynchronous processing
            try {
                kafkaTemplate.send(documentUploadTopic, savedDocument.getId().toString(), message);
                log.info("Document processing message sent successfully: {}", savedDocument.getId());
            } catch (Exception ex) {
                log.error("Failed to send document processing message: {}", ex.getMessage(), ex);
                // Update document status to FAILED
                savedDocument.setProcessingStatus(ProcessingStatus.FAILED);
                savedDocument.setProcessingError("Failed to queue document for processing: " + ex.getMessage());
                documentRepository.save(savedDocument);
            }

            log.info("Document uploaded and queued for processing: {}", savedDocument.getId());

            // Return DTO
            return mapToDTO(savedDocument);
        } catch (IOException e) {
            log.error("Error uploading document", e);
            throw new FileProcessingException(file.getOriginalFilename(), "uploading", e);
        }
    }

    @Override
    @Transactional
    //@Cacheable(value = "documentById", key = "#id")
    public DocumentDTO getDocumentById(Long id) {
        log.info("Fetching document by id: {}", id);
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        return mapToDTO(document);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id, String username) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Check if user is the uploader or an admin
        if (!document.getUploadedBy().getId().equals(user.getId()) && 
                user.getRole() != com.di.model.Role.ADMIN) {
            throw new AccessDeniedException("You don't have permission to delete this document");
        }

        try {
            // Delete from Elasticsearch first
            DocumentIndex documentIndex = documentIndexRepository.findByDatabaseId(id);
            if (documentIndex != null) {
                log.info("Deleting document from Elasticsearch. ID: {}", documentIndex.getId());
                documentIndexRepository.delete(documentIndex);
            }

            // Delete the file from storage
            if (document.getFilePath() != null) {
                log.info("Deleting file from storage: {}", document.getFilePath());
                fileStorageService.deleteFile(document.getFilePath());
            }

            // Delete from database
            log.info("Deleting document from database. ID: {}", id);
            documentRepository.delete(document);
            
            log.info("Document deleted successfully. ID: {}, Title: {}", id, document.getTitle());
        } catch (Exception e) {
            log.error("Error during document deletion. ID: {}, Error: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete document completely", e);
        }
    }

    @Override
    @Transactional
    //@Cacheable(value = "documentByAuthor", key = "#author + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<DocumentDTO> findByAuthor(String author, Pageable pageable) {
        log.info("Finding documents by author: {}", author);
        return documentRepository.findByAuthorContainingIgnoreCase(author, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    //@Cacheable(value = "documentByTitle", key = "#title + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<DocumentDTO> findByTitle(String title, Pageable pageable) {
        log.info("Finding documents by title: {}", title);
        return documentRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    //@Cacheable(value = "documentByType", key = "#documentType + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<DocumentDTO> findByDocumentType(DocumentType documentType, Pageable pageable) {
        log.info("Finding documents by type: {}", documentType);
        return documentRepository.findByDocumentType(documentType, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    //@Cacheable(value = "documentByDateRange", key = "#startDate + '_' + #endDate + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<DocumentDTO> findByUploadDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("Finding documents by upload date between {} and {}", startDate, endDate);
        return documentRepository.findByUploadDateBetween(startDate, endDate, pageable)
                .map(this::mapToDTO);
    }

    private DocumentDTO mapToDTO(Document document) {
        return DocumentDTO.builder()
                .id(document.getId())
                .title(document.getTitle())
                .fileName(document.getFileName())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .author(document.getAuthor())
                .uploadDate(document.getUploadDate())
                .lastModifiedDate(document.getLastModifiedDate())
                .uploadedBy(document.getUploadedBy().getUsername())
                .documentType(document.getDocumentType())
                .build();
    }

    private DocumentType determineDocumentType(String fileName) {
        if (fileName == null) {
            return DocumentType.OTHER;
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        switch (extension) {
            case "pdf":
                return DocumentType.PDF;
            case "docx":
            case "doc":
                return DocumentType.DOCX;
            case "txt":
                return DocumentType.TXT;
            case "rtf":
                return DocumentType.RTF;
            default:
                return DocumentType.OTHER;
        }
    }
}
