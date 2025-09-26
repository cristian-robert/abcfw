package com.spring.befwlc.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "azure")
public class AzureProperties {
    private String organization;
    private String project;
    private String pat;
}
