package com.di.model.elasticsearch;

import com.di.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * Elasticsearch document model for indexing and searching documents.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "#{@environment.getProperty('application.elasticsearch.index-name')}")
public class DocumentIndex {

    @Id
    private String id;
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;
    @Field(type = FieldType.Text, analyzer = "standard")
    private String author;
    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;
    @Field(type = FieldType.Keyword)
    private String fileName;
    @Field(type = FieldType.Keyword)
    private DocumentType documentType;
    @Field(type = FieldType.Keyword)
    private String uploadedBy;
    @Field(type = FieldType.Long)
    private Long databaseId;
}