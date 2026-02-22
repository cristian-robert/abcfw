package com.spring.befwlc.feature_methods;

import com.spring.befwlc.context.ScenarioContext;
import com.spring.befwlc.exceptions.TestExecutionException;

import java.util.List;

public interface CallableMethod {

    String execute(List<String> args, ScenarioContext scenarioContext);

    default void assertArgsCountGtThan(final List<String> args, int count){
        if(args.size() <= count){
            throw new TestExecutionException("Invalid number of arguments of '%s' feature method. Expected: args count > %s", this.toString(), count);
        }
    }

    default void assertArgsCountLeThan(final List<String> args, int count){
        if(args.size() > count){
            throw new TestExecutionException("Invalid number of arguments of '%s' feature method. Expected: args count < %s", this.toString(), count);
        }
    }

    default void assertArgsCountEquals(final List<String> args, int count){
        if(args.size() != count){
            throw new TestExecutionException("Invalid number of arguments of '%s' feature method. Expected: args count = %s", this.toString(), count);
        }
    }
}
