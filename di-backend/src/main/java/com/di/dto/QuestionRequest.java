package com.di.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {
    
    @NotBlank(message = "Question is required")
    private String question;
    
    private Integer maxResults = 5;
    
    private Integer snippetLength = 200;
}