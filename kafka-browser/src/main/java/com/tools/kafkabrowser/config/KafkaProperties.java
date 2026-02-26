package com.tools.kafkabrowser.config;

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
    private Ssl ssl = new Ssl();
    private SchemaRegistrySsl schemaRegistrySsl = new SchemaRegistrySsl();
    private Consumer consumer = new Consumer();

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
        private String truststoreLocation;
        private String truststorePassword;
        private String truststoreType = "JKS";
        private String keystoreLocation;
        private String keystorePassword;
        private String keystoreType = "PKCS12";
    }

    @Getter @Setter
    public static class Consumer {
        private String autoOffsetReset = "latest";
        private int pollTimeoutMs = 3000;
    }
}
