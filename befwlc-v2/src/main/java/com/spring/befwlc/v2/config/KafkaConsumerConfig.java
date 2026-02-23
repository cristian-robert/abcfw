package com.spring.befwlc.v2.config;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public KafkaAvroDeserializer kafkaAvroDeserializer(KafkaProperties kafkaProperties) {
        CachedSchemaRegistryClient client = new CachedSchemaRegistryClient(kafkaProperties.getSchemaRegistryUrl(), 1000);
        return new KafkaAvroDeserializer(client);
    }

    @Bean
    public ConsumerFactory<byte[], byte[]> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getAutoOffsetReset());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-engine-automation-" + System.getProperty("user.name", "default"));

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
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<byte[], byte[]> kafkaListenerContainerFactory(
            ConsumerFactory<byte[], byte[]> consumerFactory, KafkaProperties kafkaProperties) {
        ConcurrentKafkaListenerContainerFactory<byte[], byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setPollTimeout(kafkaProperties.getConsumer().getPollTimeoutMs());
        return factory;
    }
}
