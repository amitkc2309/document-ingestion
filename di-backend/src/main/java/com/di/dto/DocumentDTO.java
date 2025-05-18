package com.di.dto;

import com.di.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private String title;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String author;
    private String textContent;
    private LocalDateTime uploadDate;
    private LocalDateTime lastModifiedDate;
    private String uploadedBy;
    private DocumentType documentType;
}