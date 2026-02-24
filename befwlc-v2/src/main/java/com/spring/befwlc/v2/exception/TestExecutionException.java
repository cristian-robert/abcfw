package com.spring.befwlc.v2.exception;

public class TestExecutionException extends RuntimeException {
    public TestExecutionException(String message, Object... args) {
        super(args.length > 0 ? String.format(message, args) : message);
    }
    public TestExecutionException(String message, Exception cause) {
        super(message, cause);
    }
}
