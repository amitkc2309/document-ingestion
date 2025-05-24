package com.di.service.impl;

import com.di.dto.DocumentDTO;
import com.di.model.DocumentType;
import com.di.model.elasticsearch.DocumentIndex;
import com.di.repository.DocumentRepository;
import com.di.repository.elasticsearch.DocumentIndexRepository;
import com.di.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Implementation of SearchService that uses Elasticsearch for searching documents.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final DocumentIndexRepository documentIndexRepository;
    private final DocumentRepository documentRepository;

    @Override
    public Page<DocumentDTO> searchByTitle(String title, Pageable pageable) {
        log.info("Searching documents by title: {}", title);
        return documentIndexRepository.findByTitle(title, pageable)
                .map(this::mapToDTO);
    }

    @Override
    public Page<DocumentDTO> searchByAuthor(String author, Pageable pageable) {
        log.info("Searching documents by author: {}", author);
        return documentIndexRepository.findByAuthor(author, pageable)
                .map(this::mapToDTO);
    }

    @Override
    public Page<DocumentDTO> searchByContent(String content, Pageable pageable) {
        log.info("Searching documents by content: {}", content);
        return documentIndexRepository.findByContent(content, pageable)
                .map(this::mapToDTO);
    }

    @Override
    public Page<DocumentDTO> searchByDocumentType(DocumentType documentType, Pageable pageable) {
        log.info("Searching documents by type: {}", documentType);
        return documentIndexRepository.findByDocumentType(documentType, pageable)
                .map(this::mapToDTO);
    }

    @Override
    public Page<DocumentDTO> searchByUploadedBy(String uploadedBy, Pageable pageable) {
        log.info("Searching documents by uploader: {}", uploadedBy);
        return documentIndexRepository.findByUploadedBy(uploadedBy, pageable)
                .map(this::mapToDTO);
    }

    @Override
    public Page<DocumentDTO> searchByKeyword(String keyword, Pageable pageable) {
        log.info("Searching documents by keyword: {}", keyword);
        return documentIndexRepository.searchByKeyword(keyword, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Map an Elasticsearch document to a DTO.
     *
     * @param documentIndex The Elasticsearch document
     * @return The DTO
     */
    private DocumentDTO mapToDTO(DocumentIndex documentIndex) {
        return DocumentDTO.builder()
                .id(documentIndex.getDatabaseId())
                .title(documentIndex.getTitle())
                .fileName(documentIndex.getFileName())
                .author(documentIndex.getAuthor())
                .textContent(documentIndex.getContent())
                .uploadedBy(documentIndex.getUploadedBy())
                .documentType(documentIndex.getDocumentType())
                .build();
    }
}