package com.tools.kafkabrowser.config;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SchemaRegistryConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public SchemaRegistryClient schemaRegistryClient() {
        Map<String, Object> srConfig = KafkaSslConfigurer.buildSchemaRegistrySslConfig(kafkaProperties);
        srConfig.put("schema.registry.url", kafkaProperties.getSchemaRegistryUrl());
        SchemaRegistryClient client = new CachedSchemaRegistryClient(
                List.of(kafkaProperties.getSchemaRegistryUrl()), 1000, srConfig);
        log.info("Schema Registry client created for {}", kafkaProperties.getSchemaRegistryUrl());
        return client;
    }
}
