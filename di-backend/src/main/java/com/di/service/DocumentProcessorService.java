package com.di.service;

import com.di.dto.DocumentProcessingMessage;

/**
 * Service for processing documents asynchronously.
 */
public interface DocumentProcessorService {

    /**
     * Process a document asynchronously.
     *
     * @param message The document processing message
     */
    void processDocument(DocumentProcessingMessage message);
}