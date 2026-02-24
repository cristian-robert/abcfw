package com.spring.befwlc.v2.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "kafka")
@Validated
@Getter
@Setter
public class KafkaProperties {
    @NotBlank private String bootstrapServers;
    @NotBlank private String schemaRegistryUrl;
    @NotBlank private String securityProtocol;
    @NotBlank private String publishTopic;
    @NotBlank private String consumeTopic;
    private Ssl ssl = new Ssl();
    private SchemaRegistrySsl schemaRegistrySsl = new SchemaRegistrySsl();
    private Consumer consumer = new Consumer();
    private Producer producer = new Producer();

    @Getter @Setter
    public static class Ssl {
        private String keyStoreLocation;
        private String keyStorePassword;
        private String trustStoreLocation;
        private String trustStorePassword;
        private String keyPassword;
        private String keyStoreType = "pkcs12";
    }

    @Getter @Setter
    public static class SchemaRegistrySsl {
        private String caLocation;
        private String keystoreLocation;
        private String keystorePassword;
        private boolean enableCertificateVerification = true;
    }

    @Getter @Setter
    public static class Consumer {
        private String autoOffsetReset = "latest";
        private int pollTimeoutMs = 1000;
    }

    @Getter @Setter
    public static class Producer {
        private String acks = "all";
        private int retries = 10;
        private boolean enableIdempotence = false;
        private int maxInFlightRequestsPerConnection = 1;
    }
}
