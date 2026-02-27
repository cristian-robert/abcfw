package com.tools.kafkabrowser.model;

import java.util.List;

public record BulkProduceRequest(
        String topic,
        String schemaSubject,
        List<String> messages
) {}
