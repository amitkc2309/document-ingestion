package com.di.service;

import com.di.dto.DocumentDTO;
import com.di.dto.DocumentSearchCriteria;
import com.di.model.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public interface DocumentService {
    
    /**
     * Upload a new document
     * @param file The document file
     * @param title Document title
     * @param author Document author
     * @param username Username of the uploader
     * @return The uploaded document details
     */
    CompletableFuture<DocumentDTO> uploadDocument(MultipartFile file, String title, String author, String username);
    
    /**
     * Get a document by ID
     * @param id Document ID
     * @return The document details
     */
    DocumentDTO getDocumentById(Long id);
    
    /**
     * Delete a document by ID
     * @param id Document ID
     * @param username Username of the requester (for authorization)
     */
    void deleteDocument(Long id, String username);
    
    /**
     * Search documents by multiple criteria
     * @param criteria Search criteria
     * @param pageable Pagination and sorting information
     * @return Page of documents matching the criteria
     */
    Page<DocumentDTO> searchDocuments(DocumentSearchCriteria criteria, Pageable pageable);
    
    /**
     * Search documents by author
     * @param author Author name
     * @param pageable Pagination and sorting information
     * @return Page of documents by the author
     */
    Page<DocumentDTO> findByAuthor(String author, Pageable pageable);
    
    /**
     * Search documents by title
     * @param title Document title
     * @param pageable Pagination and sorting information
     * @return Page of documents with matching title
     */
    Page<DocumentDTO> findByTitle(String title, Pageable pageable);
    
    /**
     * Search documents by document type
     * @param documentType Document type
     * @param pageable Pagination and sorting information
     * @return Page of documents of the specified type
     */
    Page<DocumentDTO> findByDocumentType(DocumentType documentType, Pageable pageable);
    
    /**
     * Search documents by upload date range
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination and sorting information
     * @return Page of documents uploaded within the date range
     */
    Page<DocumentDTO> findByUploadDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}