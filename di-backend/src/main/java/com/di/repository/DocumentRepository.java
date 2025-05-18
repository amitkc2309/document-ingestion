package com.di.repository;

import com.di.model.Document;
import com.di.model.DocumentType;
import com.di.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Find documents by author
    Page<Document> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    // Find documents by title
    Page<Document> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Find documents by document type
    Page<Document> findByDocumentType(DocumentType documentType, Pageable pageable);

    // Find documents by upload date range
    Page<Document> findByUploadDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find documents by uploader
    Page<Document> findByUploadedBy(User uploadedBy, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.textContent LIKE CONCAT('%', :keyword, '%')")
    Page<Document> searchByContent(@Param("keyword") String keyword, Pageable pageable);

    // Combined search for Q&A API (no LOWER() on CLOB field)
    @Query("SELECT d FROM Document d WHERE " +
            "d.textContent LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Document> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Multi-criteria search
    @Query("SELECT d FROM Document d WHERE " +
            "(:title IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR LOWER(d.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
            "(:documentType IS NULL OR d.documentType = :documentType) AND " +
            "(:startDate IS NULL OR d.uploadDate >= :startDate) AND " +
            "(:endDate IS NULL OR d.uploadDate <= :endDate)")
    Page<Document> findByMultipleCriteria(
            @Param("title") String title,
            @Param("author") String author,
            @Param("documentType") DocumentType documentType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
