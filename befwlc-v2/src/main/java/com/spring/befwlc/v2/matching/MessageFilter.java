package com.spring.befwlc.v2.matching;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class MessageFilter {
    private final String key;
    private final String expectedValue;
    private Map<String, String> mismatchDetails;

    public MessageFilter withMismatch(String expected, String actual) {
        MessageFilter copy = new MessageFilter(this.key, this.expectedValue);
        copy.mismatchDetails = new LinkedHashMap<>();
        copy.mismatchDetails.put("Expected", expected);
        copy.mismatchDetails.put("Actual", actual);
        return copy;
    }
}
