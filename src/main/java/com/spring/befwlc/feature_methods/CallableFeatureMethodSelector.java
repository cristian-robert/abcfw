package com.spring.befwlc.feature_methods;

import com.spring.befwlc.exceptions.TestExecutionException;
import com.spring.befwlc.utils.TransformationUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CallableFeatureMethodSelector {

    public static String executeStaticMethod(final String methodName) {
        try {
            return StaticFeatureMethod.valueOf(methodName).value();
        } catch (IllegalArgumentException e) {
            throw new TestExecutionException(featureMethodsDescription(methodName));
        }
    }

    public static String executeCallableMethod(final String methodName, final List<String> callableMethodArgs) {
        try {
            return CallableFeatureMethod.valueOf(methodName).execute(callableMethodArgs);
        } catch (IllegalArgumentException e){
            throw new TestExecutionException(featureMethodsDescription(methodName));
        }
    }

    public static String featureMethodsDescription(final String methodName) {
        final Map<String, String> featureMethodsMap = new TreeMap<>();
        Arrays.stream(CallableFeatureMethod.values())
                .forEach(callableMethod -> featureMethodsMap.put(callableMethod.name(), callableMethod.description()));
        Arrays.stream(StaticFeatureMethod.values())
                .forEach(staticMethod -> featureMethodsMap.put(staticMethod.name(), staticMethod.description()));
        return String.format("Feature payload method is not defined: '%s'%nAvailable values: %n%s",
                methodName, TransformationUtils.objectToPrettyString(featureMethodsMap));
    }
}