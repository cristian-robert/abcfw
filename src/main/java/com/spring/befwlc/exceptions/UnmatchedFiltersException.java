package com.spring.befwlc.exceptions;

public class UnmatchedFiltersException extends TestExecutionException {
    public UnmatchedFiltersException(String message, Object... args) {
        super(message, args);
    }

    public UnmatchedFiltersException(String message, Exception e) {
        super(message, e);
    }
}
