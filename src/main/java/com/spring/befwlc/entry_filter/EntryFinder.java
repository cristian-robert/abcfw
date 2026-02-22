package com.spring.befwlc.entry_filter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.context.ScenarioContext;
import com.spring.befwlc.context.ScenarioContextKeys;
import com.spring.befwlc.entry_filter.json.JsonNodeHelper;
import com.spring.befwlc.entry_filter.json.JsonNodeInstance;
import com.spring.befwlc.exceptions.TestExecutionException;
import com.spring.befwlc.utils.TransformationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Slf4j
@Component
public class EntryFinder {

    private static void logEntriesFound(final List<ObjectNode> entries) {
        log.info("Entry found: {}", TransformationUtils.objectToPrettyString(entries));
    }

    public static void logPartiallyMatchedEntries(final List<ObjectNode> entries, final List<PartiallyMatchedEntry> partiallyMatchedMessages, final boolean matchedFlag) {
        if (partiallyMatchedMessages.isEmpty()) {
            log.error("No entry found by given filters");
        } else {
            partiallyMatchedMessages.sort(Comparator.comparingInt(o -> o.getUnmatchedFilters().size()));
            for (final PartiallyMatchedEntry partiallyMatchedMessage : partiallyMatchedMessages) {
                if (entries.contains(partiallyMatchedMessage.getEntry())) {
                    final int matchedCount = partiallyMatchedMessage.getMatchedFilters().size();
                    final int totalCount = partiallyMatchedMessage.getUnmatchedFilters().size() + matchedCount;
                    final String resultMessage = "Partially matched entry found, ({}/{} filters matched: {})";

                    if (matchedFlag) {
                        log.warn(resultMessage, matchedCount, totalCount, partiallyMatchedMessage.toMap());
                    } else {
                        log.info(resultMessage, matchedCount, totalCount, partiallyMatchedMessage.toMap());
                    }
                }
            }
        }
    }

    public static boolean entryFoundByFilters(final List<ObjectNode> entries, final EntryFilters entryFilters, final ScenarioContext scenarioContext) {
        final List<ObjectNode> entriesFound = EntryFinder.findEntryByFilters(entries, entryFilters, true);

        if (entriesFound.isEmpty()) {
            return false;
        } else if (entriesFound.size() > 1) {
            throw new TestExecutionException("Multiple entries found:\n" + TransformationUtils.objectToPrettyString(entriesFound));
        } else {
            scenarioContext.put(ScenarioContextKeys.LAST_MATCHED_RECORD, entriesFound.get(0));
            EntryFinder.logEntriesFound(entriesFound);
            return true;
        }
    }

    public static boolean entryNotFoundByFilters(final List<ObjectNode> entries, final EntryFilters entryFilters) {
        final List<ObjectNode> entriesFound = EntryFinder.findEntryByFilters(entries, entryFilters, true);

        if (!entriesFound.isEmpty()) {
            throw new TestExecutionException("Entries found:\n" + TransformationUtils.objectToPrettyString(entriesFound));
        }

        return false;
    }

    public static List<ObjectNode> findEntryByFilters(final List<ObjectNode> entries, final EntryFilters entryFilters, final boolean removeMessages) {
        final List<ObjectNode> toRemove = new ArrayList<>();
        final List<ObjectNode> results = new ArrayList<>();

        for (ObjectNode jsonNode : entries) {
            if (filtersMatched(jsonNode, entryFilters)) {
                if (removeMessages) {
                    toRemove.add(jsonNode);
                }
                results.add(jsonNode);
            }
        }

        entries.removeAll(toRemove);
        return results;
    }

    public static void matchObjectNodes(final List<ObjectNode> expectedNodes, final List<ObjectNode> actualNodes) {
        log.info("Expected object: {}", TransformationUtils.objectToPrettyString(expectedNodes));
        log.info("Actual object: {}", TransformationUtils.objectToPrettyString(actualNodes));
        if (!ObjectUtils.nullSafeEquals(expectedNodes, actualNodes)) {
            throw new TestExecutionException("Failed to match objects.\n\nExpected:\n\nActual:\n",
                    TransformationUtils.objectToPrettyString(expectedNodes),
                    TransformationUtils.objectToPrettyString(actualNodes));
        } else {
            log.info("Objects successfully matched");
        }
    }

    private static boolean filtersMatched(final ObjectNode rootNode, final EntryFilters entryFilters) {
        final List<EntryFilter> initialFilters = entryFilters.getInitialFilters();
        final List<EntryFilter> unmatchedFilters = new ArrayList<>();
        final List<EntryFilter> matchedFilters = new ArrayList<>();

        for (final EntryFilter initialFilter : initialFilters) {
            final String value = initialFilter.getValue();
            final String key = initialFilter.getKey();
            final JsonNodeInstance currentNodeInstance = JsonNodeHelper.extractNodeInstanceByKey(rootNode, key);
            final String currentNodeInstanceValue = currentNodeInstance.getNodeValue().asText();
            final boolean isCurrentNodeFound = !currentNodeInstance.isNodeNotFound();
            final boolean isNodeValueMatched = JsonNodeHelper.valuesMatches(value, currentNodeInstanceValue);
            final boolean isMatched = isCurrentNodeFound && isNodeValueMatched;

            if (isMatched) {
                matchedFilters.add(initialFilter);
            } else {
                final Map<String, String> unmatchedDetails = new LinkedHashMap<>();
                unmatchedDetails.put("Expected", value);

                if (!isCurrentNodeFound) {
                    unmatchedDetails.put("Actual", String.format("Node '%s' not found", key));
                } else {
                    unmatchedDetails.put("Actual", currentNodeInstanceValue);
                }

                final EntryFilter unmatchedFilter = new EntryFilter(key, value);
                unmatchedFilter.setId(initialFilter.getId());
                unmatchedFilter.setUnmatchedDetails(unmatchedDetails);
                unmatchedFilters.add(unmatchedFilter);
            }
        }

        entryFilters.addPartiallyMatchedMessage(matchedFilters, unmatchedFilters, rootNode);
        return unmatchedFilters.isEmpty();
    }
}