package com.tools.kafkabrowser.model;

public record ProduceRequest(
        String topic,
        String schemaSubject,
        String jsonContent
) {}
