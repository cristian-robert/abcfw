package com.spring.befwlc.entry_filter;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Getter
@Setter
public class EntryFilter {

    private final String key;
    private UUID id;
    private String value;
    private Map<String, String> unmatchedDetails = new TreeMap<>();

    public EntryFilter(final String key, final String value){
        this.key = key;
        this.value = value;
        this.id = UUID.randomUUID();
    }

}
