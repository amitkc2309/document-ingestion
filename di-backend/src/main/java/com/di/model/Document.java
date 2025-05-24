package com.di.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String author;

    // Path to the stored file in the file system
    @Column
    private String filePath;

    // Elasticsearch document ID for this document
    @Column
    private String elasticsearchId;

    // Processing status for asynchronous processing
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    // Processing error message if any
    @Column
    private String processingError;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @Column
    private LocalDateTime lastModifiedDate;

    @Column
    private LocalDateTime processedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;
}
