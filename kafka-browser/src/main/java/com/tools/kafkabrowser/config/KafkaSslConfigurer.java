package com.tools.kafkabrowser.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Static SSL configuration helpers — mirrors befwlc-v2 KafkaConsumerConfig SSL methods.
 */
public final class KafkaSslConfigurer {

    private KafkaSslConfigurer() {}

    public static void addKafkaSslProperties(Map<String, Object> props, KafkaProperties kafkaProperties) {
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
    }

    public static void addSchemaRegistrySslProperties(Map<String, Object> props, KafkaProperties kafkaProperties) {
        KafkaProperties.SchemaRegistrySsl srSsl = kafkaProperties.getSchemaRegistrySsl();
        if (srSsl.getTruststoreLocation() != null) {
            props.put("schema.registry.ssl.truststore.location", srSsl.getTruststoreLocation());
            props.put("schema.registry.ssl.truststore.password", srSsl.getTruststorePassword());
            props.put("schema.registry.ssl.truststore.type", srSsl.getTruststoreType());
        }
    }

    public static Map<String, Object> buildSchemaRegistrySslConfig(KafkaProperties kafkaProperties) {
        Map<String, Object> config = new HashMap<>();
        KafkaProperties.Ssl ssl = kafkaProperties.getSsl();
        if (ssl.getTrustStoreLocation() != null) {
            config.put("schema.registry.ssl.truststore.location", ssl.getTrustStoreLocation());
            config.put("schema.registry.ssl.truststore.password", ssl.getTrustStorePassword());
            config.put("schema.registry.ssl.keystore.location", ssl.getKeyStoreLocation());
            config.put("schema.registry.ssl.keystore.password", ssl.getKeyStorePassword());
            config.put("schema.registry.ssl.keystore.type", ssl.getKeyStoreType());
            config.put("schema.registry.ssl.key.password", ssl.getKeyPassword());
        }
        return config;
    }
}
