package com.tools.kafkabrowser.model;

import java.time.LocalDateTime;

public record CollectionSummaryDto(
        Long id,
        String name,
        String topicName,
        String schemaSubject,
        boolean hasAvsc,
        int templateCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CollectionSummaryDto from(Collection collection, int templateCount) {
        return new CollectionSummaryDto(
                collection.getId(),
                collection.getName(),
                collection.getTopicName(),
                collection.getSchemaSubject(),
                collection.getAvscContent() != null && !collection.getAvscContent().isBlank(),
                templateCount,
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }
}
