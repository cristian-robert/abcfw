package com.spring.befwlc.v2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure")
@Getter
@Setter
public class AzureProperties {
    private String organization;
    private String project;
    private String pat;
    private String testPlanId;
    private String suiteId;
    private boolean updateResults = false;
}
