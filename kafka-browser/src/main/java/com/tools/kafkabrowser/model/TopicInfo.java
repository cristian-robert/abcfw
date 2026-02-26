package com.tools.kafkabrowser.model;

public record TopicInfo(String name, int partitions, boolean internal) {}
