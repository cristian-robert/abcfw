package com.spring.befwlc.v2.kafka;

import com.spring.befwlc.v2.config.AwaitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaListenerReadyGuard {
    private final KafkaListenerEndpointRegistry registry;
    private final AwaitProperties awaitProperties;

    public void waitForListeners() {
        log.info("Waiting for Kafka listener containers to be assigned partitions...");
        Awaitility.await()
                .atMost(Duration.ofSeconds(awaitProperties.getListenerTimeoutSeconds()))
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> {
                    Collection<MessageListenerContainer> containers = registry.getListenerContainers();
                    return !containers.isEmpty() && containers.stream()
                            .allMatch(c -> !c.getAssignedPartitions().isEmpty());
                });
        log.info("All Kafka listener containers are ready");
    }
}
