package com.di.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

/**
 * Configuration class for Kafka.
 */
@Configuration
public class KafkaConfig {

    @Value("${application.kafka.topics.document-upload}")
    private String documentUploadTopic;

    @Bean
    public NewTopic documentUploadTopic() {
        return TopicBuilder.name(documentUploadTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}