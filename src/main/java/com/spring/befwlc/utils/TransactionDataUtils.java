package com.spring.befwlc.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.befwlc.exceptions.TestExecutionException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.UUID;

public class TransactionDataUtils {

    public static String defaultUUID() { return UUID.randomUUID().toString(); }

    public static String UUIDWithoutSeparator() { return UUID.randomUUID().toString().replaceAll("-", ""); }

    public static String MSUUID() { return "MS" + UUIDWithoutSeparator(); }

    public static String cutOffFieldValue(String field, String pathToConfigFile) {

        LocalTime currentTime = LocalTime.now().plusHours(3);
        JsonNode configs = getConfig(pathToConfigFile);

        for (JsonNode node : configs) {
            LocalTime start = LocalTime.parse(node.get("start").asText());
            LocalTime end = LocalTime.parse(node.get("end").asText());

            if (currentTime.isAfter(start) && currentTime.isBefore(end)) {
                return node.get(field).toString().replaceFirst( "\"",  "")
                        .replaceAll( "\"$", "")
                        .replaceAll( "\\\\","");
            }
        }

        throw new TestExecutionException("Current time is outside of configured timestamps");
    }

    public static String getByProductProperty(String product, String priority, String property, String pathToConfigFile) {
        JsonNode configs = getConfig(pathToConfigFile);

        for (JsonNode node : configs) {
            if (node.get("Product_type").asText().equals(product) &&
                    node.get("priority").asText().equals(priority)) {
                return node.get(property).asText();
            }
        }

        throw new TestExecutionException("Could not find property %s of %s product with %s priority", property, product, priority);
    }

    private static JsonNode getConfig(String pathToConfigFile) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Path path = Paths.get(pathToConfigFile);
            return mapper.readTree(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            throw new TestExecutionException("Could not load config file by specified path.");
        }
    }
}