package com.spring.befwlc.entry_filter.json;

import com.fasterxml.jackson.databind.JsonNode;

import static com.spring.befwlc.utils.RegexUtils.JSON_ARRAY_INDEX_REGEX;
import static com.spring.befwlc.utils.RegexUtils.JSON_IS_ARRAY_ACCESS_REGEX;

public class JsonNodeHelper {

    private static final String CONTAINS_WILDCARD = "%";

    public static JsonNodeInstance extractNodeInstanceByKey(final JsonNode rootNode, final String key){
        final String[] keys = key.split("\\.");
        final JsonNodeInstance currentNode = new JsonNodeInstance(rootNode, false);

        for (final String keyValue : keys){
            final String trimmedKeyValue = keyValue.trim();
            if(trimmedKeyValue.matches(JSON_IS_ARRAY_ACCESS_REGEX)) {
                final String arrayKey = trimmedKeyValue.replaceAll((JSON_ARRAY_INDEX_REGEX), "");
                final int arrayIndex = Integer.parseInt(trimmedKeyValue.replaceAll("\\D", ""));
                final JsonNode arrayNode =
                        currentNode.getNodeValue().has(arrayKey) ? currentNode.getNodeValue().get(arrayKey) : currentNode.getNodeValue();
                final JsonNode nextNode = arrayNode.path(arrayIndex);
                if(nextNode.isMissingNode()){
                    currentNode.setNodeNotFound(true);
                    break;
                } else {
                    currentNode.setNodeValue(nextNode);
                }
            } else {
                if (!currentNode.getNodeValue().has(trimmedKeyValue)){
                    currentNode.setNodeNotFound(true);
                    break;
                } else {
                    currentNode.setNodeValue(currentNode.getNodeValue().get(trimmedKeyValue));
                }
            }
        }
        return currentNode;
    }
    public static boolean valuesMatches(final String filterValue, final String nodeValue){
        if(valueStartsWith(filterValue)){
            final String value = replaceLastCharacter(filterValue);
            return nodeValue.startsWith(value);
        } else if (valueEndsWith(filterValue)) {
            final String value = replaceFirstWildCard(filterValue);
            return nodeValue.endsWith(value);
        } else if (valueContains(filterValue)) {
            final String value = filterValue.replaceAll(CONTAINS_WILDCARD, "");
            return nodeValue.contains(value);
        } else {
            return filterValue.equals(nodeValue);
        }
    }

    private static String replaceFirstWildCard(final String filterValue){
        return filterValue.replaceFirst(CONTAINS_WILDCARD, "");
    }

    private static String replaceLastCharacter(final String filterValue){
        return filterValue.substring(0, filterValue.length() - 1);
    }

    private static boolean valueContains(final String filterValue){
        return filterValue.startsWith(CONTAINS_WILDCARD) && filterValue.endsWith(CONTAINS_WILDCARD);
    }

    private static boolean valueStartsWith(final String filterValue){
        return !filterValue.startsWith(CONTAINS_WILDCARD) && filterValue.endsWith(CONTAINS_WILDCARD);
    }

    private static boolean valueEndsWith(final String filterValue){
        return filterValue.startsWith(CONTAINS_WILDCARD) && !filterValue.endsWith(CONTAINS_WILDCARD);
    }
}
