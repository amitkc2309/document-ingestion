package com.di.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.di.dto.QuestionRequest;
import com.di.dto.QuestionResponse;
import com.di.dto.QuestionResponse.DocumentSnippet;
import com.di.model.elasticsearch.DocumentIndex;
import com.di.repository.elasticsearch.DocumentIndexRepository;
import com.di.service.QAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QAServiceImpl implements QAService {

    private final DocumentIndexRepository documentIndexRepository;
    private final ElasticsearchOperations elasticsearchOperations;


    @Override
    @Transactional
    public QuestionResponse processQuestion(QuestionRequest question, Pageable pageable) {
        log.info("Processing question: {}", question.getQuestion());

        Query multiMatchQuery = MultiMatchQuery.of(m -> m
                .query(question.getQuestion())
                .fields("title", "author", "content")
        )._toQuery();

        // Create NativeQuery using the query above
        NativeQuery searchQuery = new NativeQueryBuilder()
                .withQuery(multiMatchQuery)
                .withPageable(pageable)
                .build();

        // Execute search using Spring Data Elasticsearch
        SearchHits<DocumentIndex> searchHits = elasticsearchOperations.search(searchQuery, DocumentIndex.class);
        // Convert hits to your snippet DTOs using _score from ES
        List<DocumentSnippet> snippets = searchHits.getSearchHits().stream()
                .map(hit -> DocumentSnippet.builder()
                        .documentId(hit.getContent().getDatabaseId())
                        .documentTitle(hit.getContent().getTitle())
                        .author(hit.getContent().getAuthor())
                        .snippet(hit.getContent().getContent())
                        .relevanceScore((double) hit.getScore())
                        .build())
                .limit(question.getMaxResults() != null ? question.getMaxResults() : Integer.MAX_VALUE)
                .toList();

        return QuestionResponse.builder()
                .question(question.getQuestion())
                .snippets(snippets)
                .totalResults((int) searchHits.getTotalHits())
                .build();
    }

}
