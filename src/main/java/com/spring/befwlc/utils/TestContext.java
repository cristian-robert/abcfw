package com.spring.befwlc.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @deprecated Use {@link com.spring.befwlc.context.ScenarioContext} with {@link com.spring.befwlc.context.ScenarioContextKeys} instead.
 */
@Deprecated
@Component
public class TestContext {

    private final Map<String, Object> context = new HashMap<>();

    public void set(String key, Object value) {
        context.put(key, value);
    }

    public Object get(String key) {
        return context.get(key);
    }

    public String generateShortUUID(){
        return UUID.randomUUID().toString().replaceAll("-","").substring(0, 8);
    }
}
