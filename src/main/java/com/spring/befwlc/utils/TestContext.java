package com.spring.befwlc.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
