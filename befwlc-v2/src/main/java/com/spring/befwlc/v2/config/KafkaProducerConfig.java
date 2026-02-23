package com.spring.befwlc.v2.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import test.prof.events.TransactionCreated;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, TransactionCreated> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.getProducer().getAcks());
        props.put(ProducerConfig.RETRIES_CONFIG, kafkaProperties.getProducer().getRetries());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, kafkaProperties.getProducer().isEnableIdempotence());
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, kafkaProperties.getProducer().getMaxInFlightRequestsPerConnection());
        props.put("schema.registry.url", kafkaProperties.getSchemaRegistryUrl());

        KafkaProperties.Ssl ssl = kafkaProperties.getSsl();
        if (ssl.getKeyStoreLocation() != null) {
            props.put("security.protocol", kafkaProperties.getSecurityProtocol());
            props.put("ssl.keystore.location", ssl.getKeyStoreLocation());
            props.put("ssl.keystore.password", ssl.getKeyStorePassword());
            props.put("ssl.truststore.location", ssl.getTrustStoreLocation());
            props.put("ssl.truststore.password", ssl.getTrustStorePassword());
            props.put("ssl.key.password", ssl.getKeyPassword());
            props.put("ssl.keystore.type", ssl.getKeyStoreType());
        }
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, TransactionCreated> kafkaTemplate(
            ProducerFactory<String, TransactionCreated> producerFactory, KafkaProperties kafkaProperties) {
        KafkaTemplate<String, TransactionCreated> template = new KafkaTemplate<>(producerFactory);
        template.setDefaultTopic(kafkaProperties.getPublishTopic());
        return template;
    }
}
