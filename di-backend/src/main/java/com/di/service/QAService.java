package com.di.service;

import com.di.dto.DocumentDTO;
import com.di.dto.QuestionRequest;
import com.di.dto.QuestionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.concurrent.CompletableFuture;

public interface QAService {
    
    /**
     * Process a user question and find relevant documents
     * @param question The question request containing the query
     * @return A response containing matching documents and snippets
     */
    QuestionResponse processQuestion(QuestionRequest question, Pageable pageable);
    
}