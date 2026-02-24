package com.spring.befwlc.v2.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.v2.exception.TestExecutionException;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import static com.spring.befwlc.v2.kafka.KafkaConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageListener {
    private final KafkaAvroDeserializer avroDeserializer;
    private final ObjectMapper objectMapper;
    private final KafkaMessageStore messageStore;

    @KafkaListener(topics = "${kafka.consume-topic}", containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(ConsumerRecord<byte[], byte[]> record) {
        try {
            ObjectNode envelope = objectMapper.createObjectNode();
            envelope.put(TOPIC, record.topic());
            envelope.put(PARTITION, record.partition());
            envelope.put(OFFSET, record.offset());
            envelope.put(TIMESTAMP, record.timestamp());
            Object deserialized = avroDeserializer.deserialize(record.topic(), record.value());
            envelope.set(MESSAGE, objectMapper.readTree(deserialized.toString()));
            ObjectNode headers = objectMapper.createObjectNode();
            for (Header header : record.headers()) {
                byte[] bytes = header.value();
                String value = Objects.nonNull(bytes) ? new String(bytes, StandardCharsets.UTF_8) : null;
                headers.put(header.key(), value);
            }
            envelope.set(HEADERS, headers);
            messageStore.add(envelope);
            log.debug("Stored message from topic={} offset={}", record.topic(), record.offset());
        } catch (Exception e) {
            log.error("Failed to deserialize Kafka record from topic '{}' at offset {}", record.topic(), record.offset(), e);
            throw new TestExecutionException("Failed to deserialize Kafka record: %s", e.getMessage());
        }
    }
}
