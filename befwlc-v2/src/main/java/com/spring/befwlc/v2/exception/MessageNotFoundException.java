package com.spring.befwlc.v2.exception;

public class MessageNotFoundException extends TestExecutionException {
    public MessageNotFoundException(String message, Object... args) {
        super(message, args);
    }
}
