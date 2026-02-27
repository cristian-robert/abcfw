package com.tools.kafkabrowser.model;

public record ProduceResponse(
        boolean success,
        String topic,
        int partition,
        long offset,
        String error
) {}
