package com.di.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Service for handling file storage operations.
 */
public interface FileStorageService {

    /**
     * Store a file in the file system.
     *
     * @param file The file to store
     * @param documentId The ID of the document
     * @return The path where the file is stored
     * @throws IOException If an I/O error occurs
     */
    String storeFile(MultipartFile file, Long documentId) throws IOException;

    /**
     * Get a file from the file system.
     *
     * @param filePath The path of the file to retrieve
     * @return An InputStream for the file
     * @throws IOException If an I/O error occurs
     */
    InputStream getFile(String filePath) throws IOException;

    /**
     * Delete a file from the file system.
     *
     * @param filePath The path of the file to delete
     * @return true if the file was deleted, false otherwise
     */
    boolean deleteFile(String filePath);

    /**
     * Get the absolute path for a file.
     *
     * @param filePath The relative path of the file
     * @return The absolute path of the file
     */
    Path getAbsolutePath(String filePath);
}