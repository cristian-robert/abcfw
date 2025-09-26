package com.spring.befwlc.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.exceptions.TestExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransformationUtils {

    private static final ObjectMapper objectMapper;

    static{
        objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static String transformObjectToString(final Object value) {
        return value == null ? null : value.toString();
    }

    public static String objectToPrettyString(final Object object){
        if (object instanceof String){
            return object.toString();
        }

        try {
            return objectMapper.writeValueAsString(object);
        } catch (final JsonProcessingException e){
            return transformObjectToString(object);
        }
    }

    public static <T extends JsonNode> T transformObjectToJsonNode(final Object o, final Class<T> type){
        try {
            return type.cast(objectMapper.readTree(objectToPrettyString(o)));
        } catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> transformStringToMap(final String input){
        try {
            return objectMapper.readValue(input, Map.class);
        } catch (JsonProcessingException e){
            throw new TestExecutionException(e.getMessage());
        }
    }

    public static List<ObjectNode> jsonStringToObjectNodes(final String jsonString) {
        final JsonNode jsonNode = transformObjectToJsonNode(jsonString, JsonNode.class);
        List<ObjectNode> objectNodes = new ArrayList<>();
        if (jsonNode.isArray()) {
            jsonNode.forEach(node -> objectNodes.add((ObjectNode) node));
        } else {
            objectNodes.add((ObjectNode) jsonNode);
        }
        return objectNodes;
    }

    public static <T> T jsonStringToObject(final Map<String, Object> jsonString, Class<T> type){
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(jsonString), type);
        } catch (JsonProcessingException e){
            throw new TestExecutionException(e.getMessage());
        }
    }

    public static List<ObjectNode> objectsToObjectNodes(List<?> objects){
        List<ObjectNode> nodes = new ArrayList<>();
        for(Object object : objects){
            nodes.add(objectMapper.valueToTree(object));
        }
        return nodes;
    }

    public static String objectToPrettyLog(final Object object){
        return "'" + objectToPrettyString(object) + "'";
    }
}
