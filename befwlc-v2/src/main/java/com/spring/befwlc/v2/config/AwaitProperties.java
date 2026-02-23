package com.spring.befwlc.v2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "await")
@Getter
@Setter
public class AwaitProperties {
    private int timeoutSeconds = 90;
    private int pollIntervalSeconds = 1;
    private int listenerTimeoutSeconds = 60;
}
