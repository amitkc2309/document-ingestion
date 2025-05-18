package com.di.controller;

import com.di.dto.DocumentDTO;
import com.di.dto.QuestionRequest;
import com.di.dto.QuestionResponse;
import com.di.service.QAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
@Tag(name = "Q&A", description = "Q&A API for asking questions and retrieving relevant document snippets")
@SecurityRequirement(name = "bearerAuth")
public class QAController {

    private final QAService qaService;

    @PostMapping("/ask")
    @Operation(summary = "Ask a question", description = "Processes a question and returns relevant document snippets")
    public CompletableFuture<ResponseEntity<QuestionResponse>> askQuestion(
            @Valid @RequestBody QuestionRequest question,
            @PageableDefault(size = 5) Pageable pageable) {
        
        return qaService.processQuestion(question, pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/search")
    @Operation(summary = "Search by keyword", description = "Searches documents by keyword")
    public ResponseEntity<Page<DocumentDTO>> searchByKeyword(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        
        return ResponseEntity.ok(qaService.searchByKeyword(keyword, pageable));
    }

    @GetMapping("/snippets/{documentId}")
    @Operation(summary = "Extract snippets", description = "Extracts relevant snippets from a document based on a keyword")
    public ResponseEntity<QuestionResponse> extractSnippets(
            @PathVariable Long documentId,
            @RequestParam String keyword) {
        
        return ResponseEntity.ok(qaService.extractSnippets(documentId, keyword));
    }
}