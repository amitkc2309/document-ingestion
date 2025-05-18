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
public class DocumentSearchCriteria {
    private String title;
    private String author;
    private DocumentType documentType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String keyword;
}