package com.spring.befwlc.utils;

import com.spring.befwlc.exceptions.TestExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils{
    public static final String JSON_IS_ARRAY_ACCESS_REGEX = ".*\\[\\d+]$";
    public static final String JSON_ARRAY_INDEX_REGEX = "\\[.*";
    public static final String STATIC_FEATURE_METHOD_REGEX = "^(\\$[A-Z0-9_]+).*";
    public static final String STATIC_FEATURE_METHOD_NAME_REGEX = "^(\\$[A-Z0-9_]+)";
    public static final String CALLABLE_FEATURE_METHOD_REGEX= "^(\\$[A-Z0-9_]+\\(.*\\))";
    public static final String CALLABLE_FEATURE_METHOD_NAME_REGEX= "^(\\$[A-Z0-9_]+\\(";
    public static final String CALLABLE_FEATURE_METHOD_ARGS_REGEX = "(\\$[A-Z0-9_]+\\(([^,)]+(,[^,)]+)*\\))|([^,]+)";


    public static List<String> captureValues(final String input, final String regex){
        return captureValues(input, regex, 0);
    }

    public static List<String> captureValues(final String input, final String regex, final int groupIndex){
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(input);

        final List<String> values = new ArrayList<>();
        while (matcher.find()){
            final String value = matcher.group(groupIndex);
            values.add(value);
        }
        return values;
    }

    public static String captureValue(final String input, final String regex){
        final List<String> values = captureValues(input, regex, 1);
        if (values.size() != 1){
            throw new TestExecutionException("Matcher failed to extract value. Pattern: '%s', Input: '%s'", regex, input);
        }
        return values.get(0);
    }
}
