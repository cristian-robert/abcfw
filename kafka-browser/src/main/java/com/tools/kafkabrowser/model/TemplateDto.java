package com.tools.kafkabrowser.model;

public record TemplateDto(
        String name,
        String jsonContent,
        Long collectionId
) {}
