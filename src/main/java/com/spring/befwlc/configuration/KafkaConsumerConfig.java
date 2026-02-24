package com.spring.befwlc.configuration;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private StringEncryptor jasyptStringEncryptor;

    @Autowired
    private KafkaSslProperties sslProperties;

    @Value("${test.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.security.protocol}")
    private String securityProtocol;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${spring.kafka.consumer.loop-handler-interval}")
    private long kafkaPoolTimeoutInMillis;

    @Value("${spring.kafka.consumer.loop-handler-limit}")
    private int kafkaMaxIterations;

    @Value("${spring.kafka.consumer.listener-handler-interval}")
    private int kafkaListenerHandlerInterval;

    @Value("${spring.kafka.consumer.pool-timeout}")
    private int kafkaPoolTimeout;

    @Value("${spring.kafka.consumer.listener-handler-limit}")
    private int kafkaListenerHandlerLimit;

    @Value("${test.kafka.registry-host}")
    private String kafkaRegistryHost;

    @Bean("kafkaAwaitHandlerConfiguration")
    public AwaitConfiguration awaitKafkaHandlerConfiguration() {
        return new AwaitConfiguration(kafkaPoolTimeoutInMillis, kafkaMaxIterations);
    }

    @Bean("kafkaListenerAwaitHandlerConfiguration")
    public AwaitConfiguration kafkaListenerAwaitHandlerConfiguration() {
        return new AwaitConfiguration(kafkaListenerHandlerInterval, kafkaListenerHandlerLimit);
    }

    @Bean
    public KafkaAvroDeserializer kafkaAvroDeserializer(){
        return new KafkaAvroDeserializer(new CachedSchemaRegistryClient(kafkaRegistryHost, 1000));
    }

    @Bean
    public ConsumerFactory<byte[], byte[]> consumerFactory() {
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

        props.put("security.protocol", securityProtocol);
        props.put("auto.offset.reset", autoOffsetReset);
        try {
            props.put("ssl.truststore.location", trustStore.getFile().getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put("group.id", "test-group");
        props.put("ssl.keystore.password", keyStorePass);
        props.put("ssl.truststore.password", trustStorePass);
        props.put("ssl.key.password", keyPass);
        props.put("session.timeout.ms", "10000");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<byte[], byte[]> kafkaListenerContainerFactory() {
        final ConcurrentKafkaListenerContainerFactory<byte[], byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setPollTimeout(kafkaPoolTimeout);
        factory.getContainerProperties().setGroupId("test-engine-automation-" + System.getProperty("user.name"));
        return factory;
    }
}
