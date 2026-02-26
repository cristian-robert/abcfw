package com.tools.kafkabrowser.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tools.kafkabrowser.config.KafkaProperties;
import com.tools.kafkabrowser.config.KafkaSslConfigurer;
import com.tools.kafkabrowser.model.KafkaMessageDto;
import com.tools.kafkabrowser.model.MessagePage;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.util.Utf8;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
public class KafkaReaderService {

    private static final byte MAGIC_BYTE = 0x0;

    private final KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;
    private final SchemaRegistryClient schemaRegistryClient;

    public KafkaReaderService(KafkaProperties kafkaProperties, ObjectMapper objectMapper) {
        this.kafkaProperties = kafkaProperties;
        this.objectMapper = objectMapper;

        Map<String, Object> srConfig = KafkaSslConfigurer.buildSchemaRegistrySslConfig(kafkaProperties);
        srConfig.put("schema.registry.url", kafkaProperties.getSchemaRegistryUrl());
        this.schemaRegistryClient = new CachedSchemaRegistryClient(
                List.of(kafkaProperties.getSchemaRegistryUrl()), 1000, srConfig);
        log.info("Schema Registry client created for {}", kafkaProperties.getSchemaRegistryUrl());
    }

    public MessagePage readMessages(String topic, int partition, long fromOffset, int limit, boolean useSchema) {
        Properties props = buildConsumerProps();

        try (KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(props)) {
            List<TopicPartition> partitions;

            if (partition >= 0) {
                partitions = List.of(new TopicPartition(topic, partition));
            } else {
                partitions = consumer.partitionsFor(topic).stream()
                        .map(pi -> new TopicPartition(topic, pi.partition()))
                        .toList();
            }

            consumer.assign(partitions);

            if (fromOffset >= 0) {
                for (TopicPartition tp : partitions) {
                    consumer.seek(tp, fromOffset);
                }
            } else {
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
                    messages.add(toDto(record, useSchema));
                }
            }

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

    private KafkaMessageDto toDto(ConsumerRecord<byte[], byte[]> record, boolean useSchema) {
        String key = record.key() != null ? new String(record.key(), StandardCharsets.UTF_8) : null;

        Map<String, String> headers = new LinkedHashMap<>();
        for (Header header : record.headers()) {
            byte[] bytes = header.value();
            headers.put(header.key(), bytes != null ? new String(bytes, StandardCharsets.UTF_8) : null);
        }

        byte[] valueBytes = record.value();
        Integer schemaId = null;
        String schemaType = null;
        String schemaName = null;
        Object message = null;
        String rawMessage = null;
        String error = null;

        if (valueBytes == null || valueBytes.length == 0) {
            rawMessage = null;
            error = "Empty value";
        } else if (valueBytes.length >= 5 && valueBytes[0] == MAGIC_BYTE) {
            // Avro wire format: [0x0][4-byte schema ID][binary data]
            schemaId = ByteBuffer.wrap(valueBytes, 1, 4).getInt();
            schemaType = "AVRO";

            // Raw message is the binary bytes as-is (lossy but readable for debugging)
            rawMessage = new String(valueBytes, StandardCharsets.UTF_8);

            if (useSchema) {
                try {
                    io.confluent.kafka.schemaregistry.ParsedSchema parsedSchema =
                            schemaRegistryClient.getSchemaById(schemaId);
                    Schema writerSchema = ((AvroSchema) parsedSchema).rawSchema();
                    schemaName = writerSchema.getFullName();
                    log.debug("Schema {} (id={}) with {} fields",
                            schemaName, schemaId, writerSchema.getFields().size());

                    GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>(writerSchema);
                    BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(
                            valueBytes, 5, valueBytes.length - 5, null);
                    GenericRecord genericRecord = reader.read(null, decoder);

                    message = avroToJson(genericRecord);
                    log.debug("Deserialized {} at offset {}", schemaName, record.offset());
                } catch (Exception e) {
                    log.error("Schema deserialization failed for topic={} offset={} schemaId={}",
                            record.topic(), record.offset(), schemaId, e);
                    error = e.getClass().getSimpleName() + ": " + e.getMessage();
                }
            }
        } else {
            // Not Avro — plain JSON or string
            schemaType = "NONE";
            rawMessage = new String(valueBytes, StandardCharsets.UTF_8);
            try {
                message = objectMapper.readTree(rawMessage);
            } catch (Exception e) {
                message = rawMessage;
            }
        }

        return new KafkaMessageDto(
                record.topic(),
                record.partition(),
                record.offset(),
                record.timestamp(),
                key,
                headers,
                schemaId,
                schemaType,
                schemaName,
                message,
                rawMessage,
                error
        );
    }

    // ---- Recursive Avro GenericRecord -> Jackson JsonNode converter ----

    private ObjectNode avroToJson(GenericRecord record) {
        ObjectNode node = objectMapper.createObjectNode();
        for (Schema.Field field : record.getSchema().getFields()) {
            Object value = record.get(field.name());
            node.set(field.name(), convertValue(value, field.schema()));
        }
        return node;
    }

    private JsonNode convertValue(Object value, Schema schema) {
        if (value == null) {
            return objectMapper.nullNode();
        }

        if (schema.getType() == Schema.Type.UNION) {
            for (Schema branch : schema.getTypes()) {
                if (branch.getType() == Schema.Type.NULL) continue;
                return convertValue(value, branch);
            }
            return objectMapper.nullNode();
        }

        return switch (schema.getType()) {
            case RECORD -> {
                if (value instanceof GenericRecord gr) {
                    yield avroToJson(gr);
                }
                yield objectMapper.getNodeFactory().textNode(value.toString());
            }
            case ARRAY -> {
                ArrayNode arr = objectMapper.createArrayNode();
                Schema elementSchema = schema.getElementType();
                if (value instanceof GenericArray<?> ga) {
                    for (Object item : ga) {
                        arr.add(convertValue(item, elementSchema));
                    }
                } else if (value instanceof Collection<?> coll) {
                    for (Object item : coll) {
                        arr.add(convertValue(item, elementSchema));
                    }
                }
                yield arr;
            }
            case MAP -> {
                ObjectNode mapNode = objectMapper.createObjectNode();
                Schema valueSchema = schema.getValueType();
                if (value instanceof Map<?, ?> map) {
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        mapNode.set(entry.getKey().toString(), convertValue(entry.getValue(), valueSchema));
                    }
                }
                yield mapNode;
            }
            case ENUM -> {
                if (value instanceof GenericEnumSymbol<?> ges) {
                    yield objectMapper.getNodeFactory().textNode(ges.toString());
                }
                yield objectMapper.getNodeFactory().textNode(value.toString());
            }
            case STRING -> {
                if (value instanceof Utf8 utf8) {
                    yield objectMapper.getNodeFactory().textNode(utf8.toString());
                }
                yield objectMapper.getNodeFactory().textNode(value.toString());
            }
            case INT -> objectMapper.getNodeFactory().numberNode(((Number) value).intValue());
            case LONG -> objectMapper.getNodeFactory().numberNode(((Number) value).longValue());
            case FLOAT -> objectMapper.getNodeFactory().numberNode(((Number) value).floatValue());
            case DOUBLE -> objectMapper.getNodeFactory().numberNode(((Number) value).doubleValue());
            case BOOLEAN -> objectMapper.getNodeFactory().booleanNode((Boolean) value);
            case BYTES -> {
                if (value instanceof ByteBuffer bb) {
                    byte[] bytes = new byte[bb.remaining()];
                    bb.duplicate().get(bytes);
                    yield objectMapper.getNodeFactory().textNode(Base64.getEncoder().encodeToString(bytes));
                }
                yield objectMapper.getNodeFactory().textNode(value.toString());
            }
            case FIXED -> {
                if (value instanceof GenericFixed gf) {
                    yield objectMapper.getNodeFactory().textNode(
                            Base64.getEncoder().encodeToString(gf.bytes()));
                }
                yield objectMapper.getNodeFactory().textNode(value.toString());
            }
            case NULL -> objectMapper.nullNode();
            default -> objectMapper.getNodeFactory().textNode(value.toString());
        };
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

        Map<String, Object> sslProps = new HashMap<>();
        KafkaSslConfigurer.addKafkaSslProperties(sslProps, kafkaProperties);
        sslProps.forEach((k, v) -> props.put(k, v.toString()));

        return props;
    }
}
