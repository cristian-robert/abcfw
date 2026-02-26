package com.tools.kafkabrowser.model;

import java.util.List;

public record MessagePage(List<KafkaMessageDto> messages, boolean hasMore) {}
