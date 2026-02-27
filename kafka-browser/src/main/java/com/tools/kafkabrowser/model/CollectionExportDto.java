package com.tools.kafkabrowser.model;

import java.util.List;

public record CollectionExportDto(
        String name,
        String topicName,
        String schemaSubject,
        String avscContent,
        List<TemplateExportDto> templates
) {
    public record TemplateExportDto(
            String name,
            String jsonContent
    ) {}

    public static CollectionExportDto from(Collection collection, List<Template> templates) {
        return new CollectionExportDto(
                collection.getName(),
                collection.getTopicName(),
                collection.getSchemaSubject(),
                collection.getAvscContent(),
                templates.stream()
                        .map(t -> new TemplateExportDto(t.getName(), t.getJsonContent()))
                        .toList()
        );
    }
}
