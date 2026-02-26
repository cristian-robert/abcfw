package com.spring.befwlc.v2.config;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import test.prof.events.TransactionCreated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public KafkaAvroSerializer kafkaAvroSerializer(KafkaProperties kafkaProperties) {
        Map<String, Object> schemaRegistryConfig = KafkaConsumerConfig.buildSchemaRegistrySslConfig(kafkaProperties);
        CachedSchemaRegistryClient client = new CachedSchemaRegistryClient(
                List.of(kafkaProperties.getSchemaRegistryUrl()), 1000, schemaRegistryConfig);
        KafkaAvroSerializer serializer = new KafkaAvroSerializer(client);
        serializer.configure(Map.of(
                "auto.register.schemas", false,
                "use.latest.version", true,
                "schema.registry.url", kafkaProperties.getSchemaRegistryUrl()
        ), false);
        return serializer;
    }

    @Bean
    public ProducerFactory<String, TransactionCreated> producerFactory(
            KafkaProperties kafkaProperties, KafkaAvroSerializer kafkaAvroSerializer) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.getProducer().getAcks());
        props.put(ProducerConfig.RETRIES_CONFIG, kafkaProperties.getProducer().getRetries());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, kafkaProperties.getProducer().isEnableIdempotence());
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, kafkaProperties.getProducer().getMaxInFlightRequestsPerConnection());
        props.put("schema.registry.url", kafkaProperties.getSchemaRegistryUrl());
        props.put("auto.register.schemas", false);
        props.put("use.latest.version", true);

        KafkaConsumerConfig.addKafkaSslProperties(props, kafkaProperties);
        KafkaConsumerConfig.addSchemaRegistrySslProperties(props, kafkaProperties);

        @SuppressWarnings("unchecked")
        Serializer<TransactionCreated> valueSerializer = (Serializer<TransactionCreated>) (Serializer<?>) kafkaAvroSerializer;
        DefaultKafkaProducerFactory<String, TransactionCreated> factory = new DefaultKafkaProducerFactory<>(props);
        factory.setValueSerializer(valueSerializer);
        return factory;
    }

    @Bean
    public KafkaTemplate<String, TransactionCreated> kafkaTemplate(
            ProducerFactory<String, TransactionCreated> producerFactory, KafkaProperties kafkaProperties) {
        KafkaTemplate<String, TransactionCreated> template = new KafkaTemplate<>(producerFactory);
        template.setDefaultTopic(kafkaProperties.getPublishTopic());
        return template;
    }
}
