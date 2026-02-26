package com.tools.kafkabrowser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.kafkabrowser.config.KafkaProperties;
import com.tools.kafkabrowser.config.KafkaSslConfigurer;
import com.tools.kafkabrowser.model.KafkaMessageDto;
import com.tools.kafkabrowser.model.MessagePage;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
public class KafkaReaderService {

    private final KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;
    private final KafkaAvroDeserializer avroDeserializer;

    public KafkaReaderService(KafkaProperties kafkaProperties, ObjectMapper objectMapper) {
        this.kafkaProperties = kafkaProperties;
        this.objectMapper = objectMapper;

        // Build Avro deserializer with mTLS Schema Registry client (same as befwlc-v2)
        Map<String, Object> srConfig = KafkaSslConfigurer.buildSchemaRegistrySslConfig(kafkaProperties);
        srConfig.put("schema.registry.url", kafkaProperties.getSchemaRegistryUrl());
        SchemaRegistryClient srClient = new CachedSchemaRegistryClient(
                List.of(kafkaProperties.getSchemaRegistryUrl()), 1000, srConfig);
        this.avroDeserializer = new KafkaAvroDeserializer(srClient);
    }

    public MessagePage readMessages(String topic, int partition, long fromOffset, int limit) {
        Properties props = buildConsumerProps();

        try (KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(props)) {
            List<TopicPartition> partitions;

            if (partition >= 0) {
                partitions = List.of(new TopicPartition(topic, partition));
            } else {
                // All partitions
                partitions = consumer.partitionsFor(topic).stream()
                        .map(pi -> new TopicPartition(topic, pi.partition()))
                        .toList();
            }

            consumer.assign(partitions);

            if (fromOffset >= 0) {
                // Seek to specific offset
                for (TopicPartition tp : partitions) {
                    consumer.seek(tp, fromOffset);
                }
            } else {
                // Seek to end-minus-limit (show latest messages)
                consumer.seekToEnd(partitions);
                for (TopicPartition tp : partitions) {
                    long endOffset = consumer.position(tp);
                    long startOffset = Math.max(0, endOffset - limit);
                    consumer.seek(tp, startOffset);
                }
            }

            List<KafkaMessageDto> messages = new ArrayList<>();
            int emptyPolls = 0;
            Duration pollTimeout = Duration.ofMillis(kafkaProperties.getConsumer().getPollTimeoutMs());

            while (messages.size() < limit && emptyPolls < 2) {
                ConsumerRecords<byte[], byte[]> records = consumer.poll(pollTimeout);
                if (records.isEmpty()) {
                    emptyPolls++;
                    continue;
                }
                emptyPolls = 0;
                for (ConsumerRecord<byte[], byte[]> record : records) {
                    if (messages.size() >= limit) break;
                    messages.add(toDto(record));
                }
            }

            // Check if there are more messages beyond what we fetched
            boolean hasMore = false;
            for (TopicPartition tp : partitions) {
                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(List.of(tp));
                if (consumer.position(tp) < endOffsets.getOrDefault(tp, 0L)) {
                    hasMore = true;
                    break;
                }
            }

            return new MessagePage(messages, hasMore);
        }
    }

    private KafkaMessageDto toDto(ConsumerRecord<byte[], byte[]> record) {
        // Deserialize key
        String key = record.key() != null ? new String(record.key(), StandardCharsets.UTF_8) : null;

        // Extract headers
        Map<String, String> headers = new LinkedHashMap<>();
        for (Header header : record.headers()) {
            byte[] bytes = header.value();
            headers.put(header.key(), bytes != null ? new String(bytes, StandardCharsets.UTF_8) : null);
        }

        // Deserialize value — try Avro first, fall back to raw string
        Object payload;
        try {
            Object deserialized = avroDeserializer.deserialize(record.topic(), record.value());
            payload = objectMapper.readTree(deserialized.toString());
        } catch (Exception e) {
            log.debug("Avro deserialization failed for topic={} offset={}, falling back to string",
                    record.topic(), record.offset());
            payload = record.value() != null ? new String(record.value(), StandardCharsets.UTF_8) : null;
        }

        return new KafkaMessageDto(
                record.topic(),
                record.partition(),
                record.offset(),
                record.timestamp(),
                key,
                headers,
                payload
        );
    }

    private Properties buildConsumerProps() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-browser-" + UUID.randomUUID());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");

        // Add SSL props via map conversion
        Map<String, Object> sslProps = new HashMap<>();
        KafkaSslConfigurer.addKafkaSslProperties(sslProps, kafkaProperties);
        sslProps.forEach((k, v) -> props.put(k, v.toString()));

        return props;
    }
}
