package com.spring.befwlc.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.kafka.ssl")
@Getter
@Setter
public class KafkaSslProperties {
    private String keyStorePassword;
    private String trustStorePassword;
    private String keyPassword;
    private String keyStoreLocation;
    private String trustStoreLocation;
    private String keyStoreType;
}
