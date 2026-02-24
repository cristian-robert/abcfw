package com.spring.befwlc.v2.kafka;

import com.spring.befwlc.v2.exception.TestExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import test.prof.events.TransactionCreated;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, TransactionCreated> kafkaTemplate;

    public void sendMessage(TransactionCreated message) {
        log.info("Publishing message to Kafka default topic");
        CompletableFuture<SendResult<String, TransactionCreated>> future = kafkaTemplate.sendDefault(message);
        try {
            SendResult<String, TransactionCreated> result = future.get(30, TimeUnit.SECONDS);
            log.info("Published to topic={} partition={} offset={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (Exception e) {
            throw new TestExecutionException("Kafka publish failed: %s", e.getMessage());
        }
    }
}
