package com.tools.kafkabrowser.model;

public record CollectionDto(
        String name,
        String topicName,
        String schemaSubject,
        String avscContent
) {}
