package com.tools.kafkabrowser.model;

import java.time.LocalDateTime;

public record TemplateSummaryDto(
        Long id,
        String name,
        String topicName,
        String schemaSubject,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TemplateSummaryDto from(Template template) {
        return new TemplateSummaryDto(
                template.getId(),
                template.getName(),
                template.getTopicName(),
                template.getSchemaSubject(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
