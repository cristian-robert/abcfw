package com.spring.befwlc.configuration;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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

    @Autowired
    private KafkaSslProperties sslProperties;

    @Value("${test.kafka.registry-host}")
    private String schemaRegistry;

    @Value("${billingEngine.topic}")
    private String billingEngineTopic;

    @Value("${spring.kafka.security.protocol}")
    private String securityProtocol;

    @Value("${test.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, TransactionCreated> producerFactory(){
        Resource trustStore = resourceLoader.getResource("file:" + System.getProperty("user.dir") + sslProperties.getTrustStoreLocation());
        String trustStorePass = jasyptStringEncryptor.decrypt(sslProperties.getTrustStorePassword());
        Resource keyStore = resourceLoader.getResource("file:" + System.getProperty("user.dir") + sslProperties.getKeyStoreLocation());
        String keyStorePass = jasyptStringEncryptor.decrypt(sslProperties.getKeyStorePassword());
        String keyPass = jasyptStringEncryptor.decrypt(sslProperties.getKeyPassword());

        Map<String, Object> props = new HashMap<>();
        props.put("ssl.keystore.type", sslProperties.getKeyStoreType());
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
