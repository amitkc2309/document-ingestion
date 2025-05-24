package com.di.controller;

import com.di.dto.DocumentDTO;
import com.di.dto.DocumentSearchCriteria;
import com.di.dto.PageResponse;
import com.di.model.DocumentType;
import com.di.service.DocumentService;
import com.di.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document API for uploading, searching, and managing documents")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document", description = "Uploads a new document to the system")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<DocumentDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            Authentication authentication) {

        return ResponseEntity.ok(documentService.uploadDocument(file, title, author, authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID", description = "Retrieves a document by its ID")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document", description = "Deletes a document by its ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, Authentication authentication) {
        documentService.deleteDocument(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search documents", description = "Searches documents by multiple criteria with pagination and sorting")
    public ResponseEntity<PageResponse<DocumentDTO>> searchDocuments(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) DocumentType documentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        DocumentSearchCriteria criteria = DocumentSearchCriteria.builder()
                .title(title)
                .author(author)
                .documentType(documentType)
                .build();
        Page<DocumentDTO> result = documentService.searchDocuments(criteria, pageable);


        PageResponse<DocumentDTO> response = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-author")
    @Operation(summary = "Find documents by author", description = "Retrieves documents by author name")
    public ResponseEntity<PageResponse<DocumentDTO>> findByAuthor(
            @RequestParam String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<DocumentDTO> result = documentService.findByAuthor(author, pageable);

        PageResponse<DocumentDTO> response = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-title")
    @Operation(summary = "Find documents by title", description = "Retrieves documents by title")
    public ResponseEntity<PageResponse<DocumentDTO>> findByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<DocumentDTO> result = documentService.findByTitle(title, pageable);

        PageResponse<DocumentDTO> response = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/by-type")
    @Operation(summary = "Find documents by type", description = "Retrieves documents by document type")
    public ResponseEntity<PageResponse<DocumentDTO>> findByDocumentType(
            @RequestParam DocumentType documentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<DocumentDTO> result = documentService.findByDocumentType(documentType, pageable);

        PageResponse<DocumentDTO> response = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-date-range")
    @Operation(summary = "Find documents by date range", description = "Retrieves documents uploaded within a date range")
    public ResponseEntity<PageResponse<DocumentDTO>> findByUploadDateBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<DocumentDTO> result = documentService.findByUploadDateBetween(startDate,endDate,pageable);

        PageResponse<DocumentDTO> response = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }
}
