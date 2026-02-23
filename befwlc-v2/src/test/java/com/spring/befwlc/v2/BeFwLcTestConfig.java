package com.spring.befwlc.v2;

import com.spring.befwlc.v2.config.AwaitProperties;
import com.spring.befwlc.v2.config.AzureProperties;
import com.spring.befwlc.v2.config.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = "com.spring.befwlc.v2")
@EnableConfigurationProperties({
        KafkaProperties.class,
        AwaitProperties.class,
        AzureProperties.class
})
public class BeFwLcTestConfig {
}
