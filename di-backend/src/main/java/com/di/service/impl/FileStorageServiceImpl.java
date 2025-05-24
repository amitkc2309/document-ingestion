package com.di.service.impl;

import com.di.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Implementation of FileStorageService that stores files in the local file system.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${application.storage.location}")
    private String storageLocation;

    /**
     * Initialize the storage directory.
     */
    @PostConstruct
    public void init() {
        try {
            Path storagePath = Paths.get(storageLocation);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                log.info("Created storage directory: {}", storagePath);
            }
        } catch (IOException e) {
            log.error("Could not initialize storage location", e);
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, Long documentId) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file");
        }

        // Create a unique filename to avoid collisions
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // Use document ID and a UUID to ensure uniqueness
        String filename = documentId + "_" + UUID.randomUUID() + fileExtension;
        
        // Create the full path
        Path destinationPath = Paths.get(storageLocation).resolve(filename).normalize();
        
        // Copy the file to the destination
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file: {}", destinationPath);
            return filename;
        }
    }

    @Override
    public InputStream getFile(String filePath) throws IOException {
        Path path = Paths.get(storageLocation).resolve(filePath).normalize();
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        return Files.newInputStream(path);
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(storageLocation).resolve(filePath).normalize();
            if (!Files.exists(path)) {
                log.warn("File not found for deletion: {}", filePath);
                return false;
            }
            Files.delete(path);
            log.info("Deleted file: {}", path);
            return true;
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    @Override
    public Path getAbsolutePath(String filePath) {
        return Paths.get(storageLocation).resolve(filePath).normalize();
    }
}