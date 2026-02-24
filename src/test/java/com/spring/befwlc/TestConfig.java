package com.spring.befwlc;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(MainConfig.class)
@ComponentScan(basePackages = {"com.spring.befwlc"})
public class TestConfig {
}
