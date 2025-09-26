package com.spring.befwlc.entry_filter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.exceptions.TestExecutionException;
import com.spring.befwlc.payload.PayloadHelper;
import com.spring.befwlc.utils.TransformationUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class EntryFilters {

    private final List<EntryFilter> initialFilters = new ArrayList<>();
    private final List<PartiallyMatchedEntry> partiallyMatchedEntries = new ArrayList<>();
    private final List<String> mandatoryFilters = new ArrayList<>();

    public EntryFilters(){
    }

    public EntryFilters(final Map<String, String> dataTable){
        initialFilters.addAll(mergeDatatableAndPayloadValues(dataTable));
    }

    public EntryFilters addFilter(final String key, final String value){
        final EntryFilter entryFilter = new EntryFilter(key, value);
        initialFilters.add(entryFilter);
        return this;
    }

    public EntryFilter getFilterByKey(final String key){
        final List<EntryFilter> filters = initialFilters.stream().filter(entryFilter -> entryFilter.getKey().equals(key))
                .collect(Collectors.toList());
        if (filters.size() != 1){
            throw new TestExecutionException("No such filter found:%s", key);
        }
        return filters.get(0);
    }
    private List<EntryFilter> mergeDatatableAndPayloadValues(final Map<String, String> dataTable){
        return dataTable.entrySet().stream().map(entry -> new EntryFilter(entry.getKey(), PayloadHelper.generateValue(entry.getValue())))
                .collect(Collectors.toList());
    }

    public void addPartiallyMatchedMessage(final List<EntryFilter> matchedFilters, final List<EntryFilter> unmatchedFilters,
                                           final ObjectNode kafkaMessage) {
        final boolean isUnmatchedFilters = unmatchedFilters.size() != initialFilters.size();
        final boolean hasMessage = partiallyMatchedEntries.stream()
                .anyMatch(partiallyMatchedEntry -> partiallyMatchedEntry.getEntry().equals(kafkaMessage));

        if (isUnmatchedFilters && !hasMessage) {
            partiallyMatchedEntries.add(new PartiallyMatchedEntry(matchedFilters, unmatchedFilters, kafkaMessage));
        }
    }

    public synchronized void assertEntryHasFilterWithKey(final String key){
        final boolean hasEntry = initialFilters.stream().anyMatch(filter -> filter.getKey().equals(key));
        if(!hasEntry){
            throw new TestExecutionException("Filter is mandatory: %s", key);
        }
    }

    public List<PartiallyMatchedEntry> getPartiallyMatchedEntriesWithMatchedKeys(final String[] uniqueMatchedKeys){
        final List<String> matchedKeys = Arrays.asList(uniqueMatchedKeys);
        if(matchedKeys.isEmpty()){
            return partiallyMatchedEntries;
        }

        log.info("Unique Kafka message keys: {}", TransformationUtils.objectToPrettyString(matchedKeys));
        return partiallyMatchedEntries.stream()
                .filter(partiallyMatchedEntry -> partiallyMatchedEntry.getMatchedFilters().stream()
                        .anyMatch(entryFilter -> matchedKeys.contains(entryFilter.getKey()) && !Objects.equals(entryFilter.getValue(), "null")))
                .collect(Collectors.toList());
    }

    private String filtersToString(final List<EntryFilter> filters){
        final StringBuilder stringBuilder = new StringBuilder();
        final Iterator<EntryFilter> iterator = filters.listIterator();

        while(iterator.hasNext()){
            final EntryFilter filter = iterator.next();
            stringBuilder.append("  ").append(filter.getKey()).append(" : ").append(filter.getValue());
            if(iterator.hasNext()){
                stringBuilder.append(",\n");
            }
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return filtersToString(initialFilters);
    }
}
