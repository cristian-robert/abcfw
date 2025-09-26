package com.spring.befwlc.entry_filter.json;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsonNodeInstance {

    private JsonNode nodeValue;
    private boolean nodeNotFound;

    public JsonNodeInstance(final JsonNode nodeValue, final boolean nodeNotFound) {
        this.nodeValue = nodeValue;
        this.nodeNotFound = nodeNotFound;
    }
}
