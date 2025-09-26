package com.spring.befwlc.configuration;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import test.prof.events.TransactionCreated;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private StringEncryptor jasyptStringEncryptor;

    @Value("${test.kafka.registry-host}")
    private String schemaRegistry;

    @Value("${billingEngine.topic}")
    private String billingEngineTopic;

    @Value("${spring.kafka.ssl.trust-store-password}")
    private String trustStorePassword;

    @Value("${spring.kafka.ssl.key-password}")
    private String keyPassword;

    @Value("${spring.kafka.security.protocol}")
    private String securityProtocol;

    @Value("${spring.kafka.ssl.key-store-password}")
    private String keyStorePassword;

    @Value("${spring.kafka.ssl.key-store-location}")
    private String keystoreLocation;

    @Value("${spring.kafka.ssl.trust-store-location}")
    private String kafkaTruststoreLocation;

    @Value("${spring.kafka.ssl.key-store-type}}")
    private String kafkaKeystoreType;

    @Value("${test.kafka.bootstrap-servers}")
    private long bootstrapServers;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Bean
    public ProducerFactory<String, TransactionCreated> producerFactory(){
        Resource trustStore = resourceLoader.getResource("file:" + System.getProperty("user.dir") + kafkaTruststoreLocation);
        String trustStorePass = jasyptStringEncryptor.decrypt(trustStorePassword);
        Resource keyStore = resourceLoader.getResource("file:" + System.getProperty("user.dir") + keystoreLocation);
        String keyStorePass = jasyptStringEncryptor.decrypt(keyStorePassword);
        String keyPass = jasyptStringEncryptor.decrypt(keyPassword);

        Map<String, Object> props = new HashMap<>();
        props.put("ssl.keystore.type", kafkaKeystoreType);
        try {
            props.put("ssl.keystore.location", keyStore.getFile().getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            props.put("ssl.truststore.location", trustStore.getFile().getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        props.put("ssl.keystore.password", keyStorePass);
        props.put("ssl.truststore.password", trustStorePass);
        props.put("ssl.key.password", keyPass);
        props.put("security.protocol", securityProtocol);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put("schema.registry.url", schemaRegistry);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, TransactionCreated> kafkaTemplate(){
        KafkaTemplate<String, TransactionCreated> kafkaTemplate = new KafkaTemplate<>(producerFactory());
        kafkaTemplate.setDefaultTopic(billingEngineTopic);
        return kafkaTemplate;
    }
}