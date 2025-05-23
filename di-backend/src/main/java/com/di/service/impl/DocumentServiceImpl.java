package com.di.service.impl;

import com.di.dto.DocumentDTO;
import com.di.dto.DocumentSearchCriteria;
import com.di.exception.FileProcessingException;
import com.di.exception.ResourceNotFoundException;
import com.di.model.Document;
import com.di.model.DocumentType;
import com.di.model.User;
import com.di.repository.DocumentRepository;
import com.di.repository.UserRepository;
import com.di.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DocumentDTO uploadDocument(MultipartFile file, String title, String author, String username) {
        try {
            // Get user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

            // Extract text content based on file type
            String textContent = extractTextFromFile(file);

            // Determine document type
            DocumentType documentType = determineDocumentType(file.getOriginalFilename());

            // Create document entity
            Document document = Document.builder()
                    .title(title)
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .author(author)
                    .textContent(textContent)
                    .uploadDate(LocalDateTime.now())
                    .uploadedBy(user)
                    .documentType(documentType)
                    .build();

            // Save document
            Document savedDocument = documentRepository.save(document);

            // Return DTO
            return mapToDTO(savedDocument);
        } catch (IOException e) {
            log.error("Error uploading document", e);
            throw new FileProcessingException(file.getOriginalFilename(), "uploading", e);
        }
    }

    @Override
    @Transactional
    public DocumentDTO getDocumentById(Long id) {
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

        documentRepository.delete(document);
    }

    @Override
    @Transactional
    public Page<DocumentDTO> searchDocuments(DocumentSearchCriteria criteria, Pageable pageable) {
        return documentRepository.findByMultipleCriteria(
                criteria.getTitle(),
                criteria.getAuthor(),
                criteria.getDocumentType(),
                pageable
        ).map(this::mapToDTO);
    }

    @Override
    @Transactional
    public Page<DocumentDTO> findByAuthor(String author, Pageable pageable) {
        return documentRepository.findByAuthorContainingIgnoreCase(author, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public Page<DocumentDTO> findByTitle(String title, Pageable pageable) {
        return documentRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public Page<DocumentDTO> findByDocumentType(DocumentType documentType, Pageable pageable) {
        return documentRepository.findByDocumentType(documentType, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public Page<DocumentDTO> findByUploadDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
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
                .textContent(document.getTextContent())
                .uploadDate(document.getUploadDate())
                .lastModifiedDate(document.getLastModifiedDate())
                .uploadedBy(document.getUploadedBy().getUsername())
                .documentType(document.getDocumentType())
                .build();
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return "";
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        switch (extension) {
            case "pdf":
                return extractTextFromPdf(file);
            case "docx":
                return extractTextFromDocx(file);
            case "xlsx":
                return extractTextFromXlsx(file);
            case "txt":
                return extractTextFromTxt(file);
            default:
                return "";
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    private String extractTextFromXlsx(MultipartFile file) throws IOException {
        StringBuilder text = new StringBuilder();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                Iterator<Row> rowIterator = sheet.iterator();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        text.append(cell.toString()).append(" ");
                    }
                    text.append("\n");
                }
            }
        }
        return text.toString();
    }

    private String extractTextFromTxt(MultipartFile file) throws IOException {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
        }
        return text.toString();
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
