package com.tools.kafkabrowser.model;

import java.util.List;

public record BulkProduceResponse(
        int total,
        int succeeded,
        int failed,
        List<ProduceResponse> results
) {}
