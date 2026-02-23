package com.spring.befwlc.v2.matching;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.v2.context.ScenarioContext;
import com.spring.befwlc.v2.dsl.DslResolver;
import lombok.Getter;
import java.util.*;

@Getter
public class MessageFilterSet {
    private final List<MessageFilter> filters = new ArrayList<>();
    private final List<MatchResult> partialMatches = new ArrayList<>();

    public MessageFilterSet() {}

    public MessageFilterSet(Map<String, String> dataTable, DslResolver dslResolver, ScenarioContext context) {
        dataTable.forEach((key, value) -> {
            String resolved = dslResolver.resolve(value, context);
            filters.add(new MessageFilter(key, resolved));
        });
    }

    public MessageFilterSet addFilter(String key, String value) {
        filters.add(new MessageFilter(key, value));
        return this;
    }

    public void addPartialMatch(MatchResult result) {
        boolean alreadyTracked = partialMatches.stream()
                .anyMatch(existing -> existing.getMessage().equals(result.getMessage()));
        boolean isPartial = result.getUnmatched().size() != filters.size();
        if (isPartial && !alreadyTracked) partialMatches.add(result);
    }

    public List<MatchResult> getBestPartialMatches() {
        List<MatchResult> sorted = new ArrayList<>(partialMatches);
        sorted.sort(Comparator.comparingInt(r -> r.getUnmatched().size()));
        return sorted;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MessageFilter f : filters) sb.append("  ").append(f.getKey()).append(" : ").append(f.getExpectedValue()).append("\n");
        return sb.toString();
    }
}
