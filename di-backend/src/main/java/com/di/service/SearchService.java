package com.di.service;

import com.di.dto.DocumentDTO;
import com.di.model.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for searching documents using Elasticsearch.
 */
public interface SearchService {

    /**
     * Search documents by title.
     *
     * @param title    The title to search for
     * @param pageable Pagination information
     * @return A page of matching documents
     */
    Page<DocumentDTO> searchByTitle(String title, Pageable pageable);

    /**
     * Search documents by author.
     *
     * @param author   The author to search for
     * @param pageable Pagination information
     * @return A page of matching documents
     */
    Page<DocumentDTO> searchByAuthor(String author, Pageable pageable);

    /**
     * Search documents by content.
     *
     * @param content  The content to search for
     * @param pageable Pagination information
     * @return A page of matching documents
     */
    Page<DocumentDTO> searchByContent(String content, Pageable pageable);

    /**
     * Search documents by document type.
     *
     * @param documentType The document type to search for
     * @param pageable     Pagination information
     * @return A page of matching documents
     */
    Page<DocumentDTO> searchByDocumentType(DocumentType documentType, Pageable pageable);

    /**
     * Search documents by uploader.
     *
     * @param uploadedBy The username of the uploader
     * @param pageable   Pagination information
     * @return A page of matching documents
     */
    Page<DocumentDTO> searchByUploadedBy(String uploadedBy, Pageable pageable);

    /**
     * Search documents by keyword in title, author, or content.
     *
     * @param keyword  The keyword to search for
     * @param pageable Pagination information
     * @return A page of matching documents
     */
    Page<DocumentDTO> searchByKeyword(String keyword, Pageable pageable);
}