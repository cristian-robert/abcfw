package com.spring.befwlc.exceptions;

public class MaximumIterationExceededException extends TestExecutionException {
    public MaximumIterationExceededException(String message, Object... args) {
        super(message, args);
    }
    public MaximumIterationExceededException(String message, Exception e) {
        super(message, e);
    }
}
