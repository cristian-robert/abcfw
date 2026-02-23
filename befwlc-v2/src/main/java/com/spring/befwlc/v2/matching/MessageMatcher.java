package com.spring.befwlc.v2.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.v2.exception.TestExecutionException;
import com.spring.befwlc.v2.kafka.KafkaMessageStore;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public final class MessageMatcher {
    private MessageMatcher() {}

    public static Optional<ObjectNode> findMatch(List<ObjectNode> messages, MessageFilterSet filterSet, KafkaMessageStore store) {
        List<ObjectNode> matches = new ArrayList<>();

        for (ObjectNode message : messages) {
            MatchResult result = evaluateFilters(message, filterSet);
            if (result.isFullMatch()) {
                matches.add(message);
            } else {
                filterSet.addPartialMatch(result);
            }
        }

        if (matches.isEmpty()) return Optional.empty();

        if (matches.size() > 1) {
            throw new TestExecutionException("Multiple messages matched the filters (%d found)", matches.size());
        }

        ObjectNode matched = matches.get(0);
        store.remove(matched);
        return Optional.of(matched);
    }

    public static void assertNoMatch(List<ObjectNode> messages, MessageFilterSet filterSet) {
        for (ObjectNode message : messages) {
            MatchResult result = evaluateFilters(message, filterSet);
            if (result.isFullMatch()) {
                throw new TestExecutionException("Expected no match but found a matching message");
            }
        }
    }

    public static void logPartialMatches(List<MatchResult> partialMatches) {
        if (partialMatches.isEmpty()) {
            log.error("No messages matched any filters");
            return;
        }

        for (MatchResult result : partialMatches) {
            log.warn("Partial match ({}/{} filters): {}", result.matchedCount(), result.totalFilterCount(), result.toMap());
        }
    }

    private static MatchResult evaluateFilters(ObjectNode message, MessageFilterSet filterSet) {
        List<MessageFilter> matched = new ArrayList<>();
        List<MessageFilter> unmatched = new ArrayList<>();

        for (MessageFilter filter : filterSet.getFilters()) {
            JsonNode node = JsonPathResolver.resolve(message, filter.getKey());
            boolean found = !node.isMissingNode();
            String nodeValue = found ? node.asText() : null;
            boolean valueMatched = found && WildcardMatcher.matches(filter.getExpectedValue(), nodeValue);

            if (valueMatched) {
                matched.add(filter);
            } else {
                String actual = found ? nodeValue : String.format("Node '%s' not found", filter.getKey());
                unmatched.add(filter.withMismatch(filter.getExpectedValue(), actual));
            }
        }

        return new MatchResult(matched, unmatched, message);
    }
}
