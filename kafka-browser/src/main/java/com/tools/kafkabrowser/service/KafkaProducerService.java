package com.tools.kafkabrowser.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.kafkabrowser.config.KafkaProperties;
import com.tools.kafkabrowser.config.KafkaSslConfigurer;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;
    private final SchemaRegistryClient schemaRegistryClient;
    private final KafkaProducer<String, Object> producer;

    public KafkaProducerService(KafkaProperties kafkaProperties, ObjectMapper objectMapper,
                                SchemaRegistryClient schemaRegistryClient) {
        this.kafkaProperties = kafkaProperties;
        this.objectMapper = objectMapper;
        this.schemaRegistryClient = schemaRegistryClient;
        this.producer = createProducer();
    }

    public RecordMetadata send(String topic, String schemaSubject, String jsonContent) throws Exception {
        Object value;
        if (schemaSubject != null && !schemaSubject.isBlank()) {
            value = convertToAvro(schemaSubject, jsonContent);
        } else {
            value = jsonContent;
        }

        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, value);
        Future<RecordMetadata> future = producer.send(record);
        RecordMetadata metadata = future.get(30, TimeUnit.SECONDS);
        log.info("Produced message to topic={} partition={} offset={}", metadata.topic(), metadata.partition(), metadata.offset());
        return metadata;
    }

    public List<String> listSubjects() throws Exception {
        Collection<String> subjects = schemaRegistryClient.getAllSubjects();
        return subjects.stream().sorted().toList();
    }

    private GenericRecord convertToAvro(String schemaSubject, String jsonContent) throws Exception {
        var schemaMetadata = schemaRegistryClient.getLatestSchemaMetadata(schemaSubject);
        String schemaString = schemaMetadata.getSchema();
        Schema avroSchema = new Schema.Parser().parse(schemaString);
        log.debug("Using schema {} (id={}) for subject {}", avroSchema.getFullName(), schemaMetadata.getId(), schemaSubject);

        JsonNode jsonNode = objectMapper.readTree(jsonContent);
        return jsonToGenericRecord(jsonNode, avroSchema);
    }

    private GenericRecord jsonToGenericRecord(JsonNode jsonNode, Schema schema) {
        GenericRecord record = new GenericData.Record(schema);
        for (Schema.Field field : schema.getFields()) {
            JsonNode fieldValue = jsonNode.get(field.name());
            if (fieldValue == null || fieldValue.isNull()) {
                record.put(field.name(), null);
            } else {
                record.put(field.name(), convertJsonToAvro(fieldValue, field.schema()));
            }
        }
        return record;
    }

    private Object convertJsonToAvro(JsonNode jsonNode, Schema schema) {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        }

        Schema effectiveSchema = schema;
        if (schema.getType() == Schema.Type.UNION) {
            for (Schema branch : schema.getTypes()) {
                if (branch.getType() == Schema.Type.NULL) continue;
                effectiveSchema = branch;
                break;
            }
            if (jsonNode.isNull()) return null;
        }

        return switch (effectiveSchema.getType()) {
            case RECORD -> jsonToGenericRecord(jsonNode, effectiveSchema);
            case ARRAY -> {
                List<Object> list = new ArrayList<>();
                Schema elementSchema = effectiveSchema.getElementType();
                for (JsonNode item : jsonNode) {
                    list.add(convertJsonToAvro(item, elementSchema));
                }
                yield list;
            }
            case MAP -> {
                Map<String, Object> map = new LinkedHashMap<>();
                Schema valueSchema = effectiveSchema.getValueType();
                jsonNode.fields().forEachRemaining(entry ->
                        map.put(entry.getKey(), convertJsonToAvro(entry.getValue(), valueSchema)));
                yield map;
            }
            case ENUM -> new GenericData.EnumSymbol(effectiveSchema, jsonNode.asText());
            case STRING -> jsonNode.asText();
            case INT -> jsonNode.asInt();
            case LONG -> jsonNode.asLong();
            case FLOAT -> (float) jsonNode.asDouble();
            case DOUBLE -> jsonNode.asDouble();
            case BOOLEAN -> jsonNode.asBoolean();
            case BYTES -> ByteBuffer.wrap(Base64.getDecoder().decode(jsonNode.asText()));
            case FIXED -> new GenericData.Fixed(effectiveSchema, Base64.getDecoder().decode(jsonNode.asText()));
            case NULL -> null;
            default -> jsonNode.asText();
        };
    }

    private KafkaProducer<String, Object> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", kafkaProperties.getSchemaRegistryUrl());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        Map<String, Object> sslProps = new HashMap<>();
        KafkaSslConfigurer.addKafkaSslProperties(sslProps, kafkaProperties);
        sslProps.forEach((k, v) -> props.put(k, v.toString()));

        Map<String, Object> srSslConfig = KafkaSslConfigurer.buildSchemaRegistrySslConfig(kafkaProperties);
        srSslConfig.forEach((k, v) -> props.put(k, v.toString()));

        log.info("Kafka producer created for {}", kafkaProperties.getBootstrapServers());
        return new KafkaProducer<>(props);
    }

    @PreDestroy
    public void close() {
        if (producer != null) {
            producer.close();
            log.info("Kafka producer closed");
        }
    }
}
