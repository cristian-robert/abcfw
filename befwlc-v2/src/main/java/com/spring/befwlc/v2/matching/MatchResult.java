package com.spring.befwlc.v2.matching;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class MatchResult {
    private final List<MessageFilter> matched;
    private final List<MessageFilter> unmatched;
    private final ObjectNode message;

    public boolean isFullMatch() {
        return unmatched.isEmpty();
    }

    public int matchedCount() {
        return matched.size();
    }

    public int totalFilterCount() {
        return matched.size() + unmatched.size();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("MATCHED", matched.stream().map(f -> f.getKey() + "=" + f.getExpectedValue()).toList());
        result.put("UNMATCHED", unmatched.stream().map(f -> {
            Map<String, String> details = new LinkedHashMap<>();
            details.put("key", f.getKey());
            if (f.getMismatchDetails() != null) details.putAll(f.getMismatchDetails());
            return details;
        }).toList());
        return result;
    }
}
