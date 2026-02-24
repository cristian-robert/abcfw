package com.spring.befwlc.v2.matching;

public final class WildcardMatcher {
    private static final String WILDCARD = "%";

    private WildcardMatcher() {}

    public static boolean matches(String pattern, String value) {
        if (pattern == null || value == null) return pattern == null && value == null;

        boolean startsW = pattern.startsWith(WILDCARD);
        boolean endsW = pattern.endsWith(WILDCARD);

        if (startsW && endsW) {
            return value.contains(pattern.substring(1, pattern.length() - 1));
        } else if (endsW) {
            return value.startsWith(pattern.substring(0, pattern.length() - 1));
        } else if (startsW) {
            return value.endsWith(pattern.substring(1));
        } else {
            return pattern.equals(value);
        }
    }
}
