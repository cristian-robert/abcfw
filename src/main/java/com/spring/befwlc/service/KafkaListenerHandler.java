package com.spring.befwlc.service;

import com.spring.befwlc.configuration.AwaitConfiguration;
import com.spring.befwlc.handlers.AwaitHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class KafkaListenerHandler {

    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;

    @Autowired
    @Qualifier("kafkaListenerAwaitHandlerConfiguration")
    private AwaitConfiguration awaitConfiguration;

    public synchronized void waitForListenersToConnect() {
        if (!isConnected.get()) {
            AwaitHandler.awaitTrue(() -> {
                updateListenersFlag();
                return isConnected.get();
            }, awaitConfiguration);
        }
    }

    private void updateListenersFlag() {
        endpointRegistry.getListenerContainers().forEach(listener -> {
                    isConnected.set(!Objects.requireNonNull(listener.getAssignedPartitions()).isEmpty());
                    if (isConnected.get()) {
                        log.info("Listener '{}-{}' connected to {} partitions",
                                listener.getGroupId(),
                                listener.getListenerId(),
                                listener.getAssignedPartitions().size()
                        );
                    }
                }

        );
    }
}
