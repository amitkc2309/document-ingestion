package com.di.repository.elasticsearch;

import com.di.model.DocumentType;
import com.di.model.elasticsearch.DocumentIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Elasticsearch repository for document indexing and searching.
 */
@Repository
public interface DocumentIndexRepository extends ElasticsearchRepository<DocumentIndex, String> {

    /**
     * Find documents by title (full-text search).
     *
     * @param title    The title to search for
     * @param pageable Pagination information
     * @return A page of matching documents
     */
    Page<DocumentIndex> findByTitle(String title, Pageable pageable);

    /**
     * Find documents by author (full-text search).
     *
     * @param author   The author to search for
     * @param pageable Pagination information
     * @return A page of matching documents
     */
    Page<DocumentIndex> findByAuthor(String author, Pageable pageable);

    /**
     * Find documents by content (full-text search).
     *
     * @param content  The content to search for
     * @param pageable Pagination information
     * @return A page of matching documents
     */
    Page<DocumentIndex> findByContent(String content, Pageable pageable);

    /**
     * Find documents by document type.
     *
     * @param documentType The document type to search for
     * @param pageable     Pagination information
     * @return A page of matching documents
     */
    Page<DocumentIndex> findByDocumentType(DocumentType documentType, Pageable pageable);

    /**
     * Find documents by uploader.
     *
     * @param uploadedBy The username of the uploader
     * @param pageable   Pagination information
     * @return A page of matching documents
     */
    Page<DocumentIndex> findByUploadedBy(String uploadedBy, Pageable pageable);

    /**
     * Find documents by database ID.
     *
     * @param databaseId The database ID
     * @return The matching document
     */
    DocumentIndex findByDatabaseId(Long databaseId);

    /**
     * Search documents by keyword in title, author, or content.
     *
     * @param keyword  The keyword to search for
     * @param pageable Pagination information
     * @return A page of matching documents
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title\", \"author\", \"content\"]}}")
    Page<DocumentIndex> searchByKeyword(String keyword, Pageable pageable);
}