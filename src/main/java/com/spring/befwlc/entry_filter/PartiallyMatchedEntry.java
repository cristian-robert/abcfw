package com.spring.befwlc.entry_filter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class PartiallyMatchedEntry {

    public List<EntryFilter> matchedFilters;
    public List<EntryFilter> unmatchedFilters;
    final ObjectNode entry;

    public PartiallyMatchedEntry(final List<EntryFilter> matchedFilters, final List<EntryFilter> unmatchedFilters, final ObjectNode entry){
        this.matchedFilters = matchedFilters;
        this.unmatchedFilters = unmatchedFilters;
        this.entry = entry;
    }

    public Map<String, Object> toMap(){
        final Map<String, Object> messageDetails = new LinkedHashMap<>();
        final Map<String, Object> matchedFiltersMap= new LinkedHashMap<>();
        final Map<String, Object> unmatchedFiltersMap= new LinkedHashMap<>();

        for (final EntryFilter entryFilter : matchedFilters) {
            matchedFiltersMap.put(entryFilter.getKey(), entryFilter.getValue());
        }

        for (final EntryFilter entryFilter : unmatchedFilters) {
            unmatchedFiltersMap.put(entryFilter.getKey(), entryFilter.getUnmatchedDetails());
        }

        messageDetails.put("MATCHED", matchedFiltersMap);
        messageDetails.put("UNMATCHED", unmatchedFiltersMap);
        messageDetails.put("ENTRY", entry);
        return messageDetails;
    }
}
