package com.tools.kafkabrowser.model;

import java.time.LocalDateTime;

public record TemplateDetailDto(
        Long id,
        String name,
        Long collectionId,
        String jsonContent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TemplateDetailDto from(Template template) {
        return new TemplateDetailDto(
                template.getId(),
                template.getName(),
                template.getCollection() != null ? template.getCollection().getId() : null,
                template.getJsonContent(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
