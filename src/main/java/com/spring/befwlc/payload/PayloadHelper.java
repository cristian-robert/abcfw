package com.spring.befwlc.payload;

import com.spring.befwlc.context.ScenarioContext;
import com.spring.befwlc.exceptions.TestExecutionException;
import com.spring.befwlc.feature_methods.CallableFeatureMethodSelector;
import com.spring.befwlc.utils.RegexUtils;

import java.util.ArrayList;
import java.util.List;

import static com.spring.befwlc.utils.RegexUtils.*;

public class PayloadHelper {

    public static String generateValue(final String inputValue, final ScenarioContext scenarioContext) {
        if (inputValue != null) {
            if (valueMatchesPattern(inputValue, CALLABLE_FEATURE_METHOD_REGEX)) {
                final String methodName = RegexUtils.captureValue(inputValue, CALLABLE_FEATURE_METHOD_NAME_REGEX);
                final String args = inputValue.substring(methodName.length() + 1, inputValue.length() - 1);
                final List<String> argList = RegexUtils.captureValues(args, CALLABLE_FEATURE_METHOD_ARGS_REGEX);

                final List<String> dynamicMethodArgs = new ArrayList<>();
                for (final String arg : argList) {
                    dynamicMethodArgs.add(generateValue(arg.trim(), scenarioContext));
                }
                return CallableFeatureMethodSelector.executeCallableMethod(methodName, dynamicMethodArgs, scenarioContext);
            } else if (valueMatchesPattern(inputValue, STATIC_FEATURE_METHOD_REGEX)) {
                if (valueMatchesPattern(inputValue, STATIC_FEATURE_METHOD_NAME_REGEX)) {
                    return CallableFeatureMethodSelector.executeStaticMethod(inputValue, scenarioContext);
                }
                throw new TestExecutionException("Invalid scenario method format: %s%nAvailable methods:%n%s", inputValue,
                        CallableFeatureMethodSelector.featureMethodsDescription(inputValue));
            }
            return inputValue;
        }
        return "null";
    }

    public static boolean valueMatchesPattern(final String value, final String pattern) {
        return value.matches(pattern);
    }
}
