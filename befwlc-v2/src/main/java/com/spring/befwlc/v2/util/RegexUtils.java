package com.spring.befwlc.v2.util;

import com.spring.befwlc.v2.exception.TestExecutionException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexUtils {
    public static final String JSON_IS_ARRAY_ACCESS = ".*\\[\\d+]$";
    public static final String JSON_ARRAY_INDEX = "\\[.*";
    public static final String STATIC_METHOD_PATTERN = "^(\\$[A-Z0-9_]+).*";
    public static final String STATIC_METHOD_NAME = "^(\\$[A-Z0-9_]+)";
    public static final String CALLABLE_METHOD_PATTERN = "^(\\$[A-Z0-9_]+\\(.*\\))";
    public static final String CALLABLE_METHOD_NAME = "^(\\$[A-Z0-9_]+\\()";
    public static final String CALLABLE_METHOD_ARGS = "(\\$[A-Z0-9_]+\\(([^,)]+(,[^,)]+)*\\))|([^,]+)";

    private static final Pattern JSON_IS_ARRAY_ACCESS_COMPILED = Pattern.compile(JSON_IS_ARRAY_ACCESS);
    private static final Pattern STATIC_METHOD_PATTERN_COMPILED = Pattern.compile(STATIC_METHOD_PATTERN);
    private static final Pattern CALLABLE_METHOD_PATTERN_COMPILED = Pattern.compile(CALLABLE_METHOD_PATTERN);

    private RegexUtils() {}

    public static List<String> captureAll(String input, String regex) {
        return captureAll(input, regex, 0);
    }
    public static List<String> captureAll(String input, String regex, int groupIndex) {
        Matcher matcher = Pattern.compile(regex).matcher(input);
        List<String> values = new ArrayList<>();
        while (matcher.find()) { values.add(matcher.group(groupIndex)); }
        return values;
    }
    public static String captureSingle(String input, String regex) {
        List<String> values = captureAll(input, regex, 1);
        if (values.size() != 1) {
            throw new TestExecutionException("Regex capture failed. Pattern: '%s', Input: '%s'", regex, input);
        }
        return values.get(0);
    }
    public static boolean matches(String input, String regex) {
        Pattern compiled = getCompiledPattern(regex);
        if (compiled != null) {
            return compiled.matcher(input).matches();
        }
        return Pattern.compile(regex).matcher(input).matches();
    }

    private static Pattern getCompiledPattern(String regex) {
        if (regex.equals(JSON_IS_ARRAY_ACCESS)) return JSON_IS_ARRAY_ACCESS_COMPILED;
        if (regex.equals(STATIC_METHOD_PATTERN)) return STATIC_METHOD_PATTERN_COMPILED;
        if (regex.equals(CALLABLE_METHOD_PATTERN)) return CALLABLE_METHOD_PATTERN_COMPILED;
        return null;
    }
}
