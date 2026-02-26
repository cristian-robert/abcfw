package com.tools.kafkabrowser;

import com.tools.kafkabrowser.config.KafkaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaBrowserApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaBrowserApplication.class, args);
    }
}
