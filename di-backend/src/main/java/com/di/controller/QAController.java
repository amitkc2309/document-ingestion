package com.di.controller;

import com.di.dto.QuestionRequest;
import com.di.dto.QuestionResponse;
import com.di.service.QAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
@Tag(name = "Q&A", description = "Q&A API for asking questions and retrieving relevant document snippets")
@SecurityRequirement(name = "bearerAuth")
public class QAController {

    private final QAService qaService;

    @GetMapping("/ask")
    @Operation(summary = "Ask a question", description = "Processes a question and returns relevant document snippets")
    public ResponseEntity<QuestionResponse> askQuestion(
            @Valid @RequestBody QuestionRequest question,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(qaService.processQuestion(question, pageable));
    }
}