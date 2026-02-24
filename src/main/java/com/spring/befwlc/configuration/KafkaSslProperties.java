package com.spring.befwlc.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class KafkaSslProperties {

    @Value("${spring.kafka.ssl.key-store-password}")
    private String keyStorePassword;

    @Value("${spring.kafka.ssl.trust-store-password}")
    private String trustStorePassword;

    @Value("${spring.kafka.ssl.key-password}")
    private String keyPassword;

    @Value("${spring.kafka.ssl.key-store-location}")
    private String keyStoreLocation;

    @Value("${spring.kafka.ssl.trust-store-location}")
    private String trustStoreLocation;

    @Value("${spring.kafka.ssl.key-store-type}")
    private String keyStoreType;
}
