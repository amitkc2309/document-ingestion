package com.di.model;

/**
 * Enum representing the processing status of a document.
 */
public enum ProcessingStatus {
    /**
     * Document has been uploaded but processing has not started yet
     */
    PENDING,
    
    /**
     * Document is currently being processed
     */
    PROCESSING,
    
    /**
     * Document has been successfully processed
     */
    COMPLETED,
    
    /**
     * Document processing failed
     */
    FAILED
}