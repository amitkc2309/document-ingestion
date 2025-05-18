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
     * @param pageable Pagination and sorting information
     * @return A response containing matching documents and snippets
     */
    CompletableFuture<QuestionResponse> processQuestion(QuestionRequest question, Pageable pageable);
    
    /**
     * Search for documents by keyword
     * @param keyword The keyword to search for
     * @param pageable Pagination and sorting information
     * @return Page of documents matching the keyword
     */
    Page<DocumentDTO> searchByKeyword(String keyword, Pageable pageable);
    
    /**
     * Extract relevant snippets from a document based on a keyword
     * @param documentId The document ID
     * @param keyword The keyword to search for
     * @return A response containing the document and relevant snippets
     */
    QuestionResponse extractSnippets(Long documentId, String keyword);
}