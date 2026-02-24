package com.spring.befwlc.v2.payload;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayloadMutator {
    public String setValue(String json, String path, String value) {
        String normalizedPath = path.startsWith("$") ? path : "$." + path;
        try {
            Object typedValue = inferType(value);
            return JsonPath.parse(json).set(normalizedPath, typedValue).jsonString();
        } catch (PathNotFoundException e) {
            log.error("Path not found: {}", path);
            throw new RuntimeException("Invalid JSON path: " + path, e);
        }
    }

    private Object inferType(String value) {
        if (value == null || value.equalsIgnoreCase("null") || value.equalsIgnoreCase("<null>") || value.equalsIgnoreCase("${null}")) return null;
        if (value.isEmpty()) return "";
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) return Boolean.parseBoolean(value);
        if (value.matches("-?\\d+\\.\\d+")) return Double.parseDouble(value);
        if (value.matches("-?\\d+")) return Long.parseLong(value);
        return value;
    }
}
