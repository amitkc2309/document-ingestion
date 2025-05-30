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
}
