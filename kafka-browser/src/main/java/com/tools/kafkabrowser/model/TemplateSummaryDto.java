package com.tools.kafkabrowser.model;

import java.time.LocalDateTime;

public record TemplateSummaryDto(
        Long id,
        String name,
        Long collectionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TemplateSummaryDto from(Template template) {
        return new TemplateSummaryDto(
                template.getId(),
                template.getName(),
                template.getCollection() != null ? template.getCollection().getId() : null,
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
