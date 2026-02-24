package com.spring.befwlc.v2.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import static com.spring.befwlc.v2.util.RegexUtils.JSON_ARRAY_INDEX;
import static com.spring.befwlc.v2.util.RegexUtils.JSON_IS_ARRAY_ACCESS;

public final class JsonPathResolver {
    private JsonPathResolver() {}

    public static JsonNode resolve(JsonNode root, String dotPath) {
        String[] segments = dotPath.split("\\.");
        JsonNode current = root;

        for (String segment : segments) {
            String trimmed = segment.trim();

            if (trimmed.matches(JSON_IS_ARRAY_ACCESS)) {
                String arrayKey = trimmed.replaceAll(JSON_ARRAY_INDEX, "");
                int index = Integer.parseInt(trimmed.replaceAll("\\D", ""));
                JsonNode arrayNode = current.has(arrayKey) ? current.get(arrayKey) : current;
                current = arrayNode.path(index);
            } else {
                if (!current.has(trimmed)) return MissingNode.getInstance();
                current = current.get(trimmed);
            }

            if (current.isMissingNode()) return current;
        }

        return current;
    }
}
