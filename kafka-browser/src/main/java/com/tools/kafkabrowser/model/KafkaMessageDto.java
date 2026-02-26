package com.tools.kafkabrowser.model;

import java.util.Map;

public record KafkaMessageDto(
        String topic,
        int partition,
        long offset,
        long timestamp,
        String key,
        Map<String, String> headers,
        Integer schemaId,
        String schemaType,
        String schemaName,
        Object message,
        String rawMessage,
        String error
) {}
