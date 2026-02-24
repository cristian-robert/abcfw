package com.spring.befwlc.v2.await;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.v2.config.AwaitProperties;
import com.spring.befwlc.v2.context.ContextKey;
import com.spring.befwlc.v2.context.ScenarioContext;
import com.spring.befwlc.v2.exception.MessageNotFoundException;
import com.spring.befwlc.v2.kafka.KafkaMessageStore;
import com.spring.befwlc.v2.matching.MessageFilterSet;
import com.spring.befwlc.v2.matching.MessageMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageAwaiter {

    private final KafkaMessageStore messageStore;
    private final AwaitProperties awaitProperties;
    private final ScenarioContext scenarioContext;

    public ObjectNode awaitMatch(MessageFilterSet filterSet) {
        log.info("Waiting for Kafka message matching filters:\n{}", filterSet);

        AtomicReference<ObjectNode> found = new AtomicReference<>();
        try {
            Awaitility.await()
                    .atMost(Duration.ofSeconds(awaitProperties.getTimeoutSeconds()))
                    .pollInterval(Duration.ofSeconds(awaitProperties.getPollIntervalSeconds()))
                    .until(() -> {
                        Optional<ObjectNode> match = MessageMatcher.findMatch(messageStore.getAll(), filterSet, messageStore);
                        match.ifPresent(found::set);
                        return match.isPresent();
                    });
        } catch (ConditionTimeoutException e) {
            MessageMatcher.logPartialMatches(filterSet.getBestPartialMatches());
            throw new MessageNotFoundException("No Kafka message found matching the given filters after %d seconds",
                    awaitProperties.getTimeoutSeconds());
        }

        ObjectNode matched = found.get();
        scenarioContext.put(ContextKey.LAST_MATCHED_RECORD, matched);
        log.info("Found matching message at offset {}", matched.get("Offset"));
        return matched;
    }

    public void awaitNoMatch(MessageFilterSet filterSet) {
        log.info("Verifying no Kafka message matches filters:\n{}", filterSet);

        try {
            Awaitility.await()
                    .atMost(Duration.ofSeconds(awaitProperties.getTimeoutSeconds()))
                    .pollInterval(Duration.ofSeconds(awaitProperties.getPollIntervalSeconds()))
                    .untilAsserted(() ->
                            MessageMatcher.assertNoMatch(messageStore.getAll(), filterSet)
                    );
        } catch (ConditionTimeoutException e) {
            // If assertNoMatch kept throwing (a match was found), propagate the failure
            throw new MessageNotFoundException("A matching message was found when none was expected");
        }

        log.info("Confirmed: no matching message found (as expected)");
    }
}
