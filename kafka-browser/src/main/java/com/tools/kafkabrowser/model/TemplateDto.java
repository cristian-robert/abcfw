package com.tools.kafkabrowser.model;

public record TemplateDto(
        String name,
        String topicName,
        String schemaSubject,
        String jsonContent
) {}
