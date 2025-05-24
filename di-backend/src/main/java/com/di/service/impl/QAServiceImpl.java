package com.di.service.impl;

import com.di.dto.DocumentDTO;
import com.di.dto.QuestionRequest;
import com.di.dto.QuestionResponse;
import com.di.dto.QuestionResponse.DocumentSnippet;
import com.di.model.Document;
import com.di.model.elasticsearch.DocumentIndex;
import com.di.repository.DocumentRepository;
import com.di.repository.elasticsearch.DocumentIndexRepository;
import com.di.service.DocumentService;
import com.di.service.QAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class QAServiceImpl implements QAService {

    private final DocumentIndexRepository documentIndexRepository;

    @Override
    @Transactional
    public QuestionResponse processQuestion(QuestionRequest question,Pageable pageable) {
        log.info("Processing question: {}", question.getQuestion());

        // Search for documents containing the keywords using Elasticsearch
        Page<DocumentIndex> documents = documentIndexRepository.searchByKeyword(question.getQuestion(), pageable);

        // Extract snippets from matching documents
        List<DocumentSnippet> snippets = new ArrayList<>();
        documents.forEach(document -> {
            List<String> extractedSnippets = extractSnippetsFromText(
                    document.getContent(), 
                    question.getQuestion(), 
                    question.getSnippetLength());

            // Create a snippet for each match
            for (String snippet : extractedSnippets) {
                snippets.add(DocumentSnippet.builder()
                        .documentId(document.getDatabaseId())
                        .documentTitle(document.getTitle())
                        .author(document.getAuthor())
                        .snippet(snippet)
                        .relevanceScore(calculateRelevanceScore(snippet, question.getQuestion()))
                        .build());
            }
        });

        // Sort snippets by relevance score (descending)
        snippets.sort((s1, s2) -> Double.compare(s2.getRelevanceScore(), s1.getRelevanceScore()));

        // Limit to maxResults if needed
        List<DocumentSnippet> limitedSnippets = snippets;
        if (question.getMaxResults() != null && snippets.size() > question.getMaxResults()) {
            limitedSnippets = snippets.subList(0, question.getMaxResults());
        }

        // Create response
        QuestionResponse response = QuestionResponse.builder()
                .question(question.getQuestion())
                .snippets(limitedSnippets)
                .totalResults((int) documents.getTotalElements())
                .build();

        return response;
    }

    // Helper methods

    private List<String> extractSnippetsFromText(String text, String keyword, int snippetLength) {
        List<String> snippets = new ArrayList<>();
        if (text == null || keyword == null) {
            return snippets;
        }

        // Create a pattern for the keyword (case insensitive)
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        // Find all occurrences
        while (matcher.find()) {
            int start = Math.max(0, matcher.start() - snippetLength / 2);
            int end = Math.min(text.length(), matcher.end() + snippetLength / 2);

            // Extract the snippet
            String snippet = text.substring(start, end);

            // Add ellipsis if needed
            if (start > 0) {
                snippet = "..." + snippet;
            }
            if (end < text.length()) {
                snippet = snippet + "...";
            }

            snippets.add(snippet);
        }

        return snippets;
    }

    private double calculateRelevanceScore(String snippet, String keyword) {
        // Simple relevance calculation based on keyword frequency
        String lowerSnippet = snippet.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();

        // Count occurrences of the keyword
        int count = 0;
        int index = lowerSnippet.indexOf(lowerKeyword);
        while (index != -1) {
            count++;
            index = lowerSnippet.indexOf(lowerKeyword, index + 1);
        }

        // Calculate score based on frequency and position
        double score = count;

        // Boost score if keyword appears at the beginning
        if (lowerSnippet.startsWith(lowerKeyword)) {
            score += 0.5;
        }

        return score;
    }

}
