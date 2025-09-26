package com.spring.befwlc.exceptions;

public class TestExecutionException extends RuntimeException {
    public TestExecutionException(String message, Object...args) {
        super(formatMessage(message,args));
    }
    public TestExecutionException(String message, final Exception e){
        super(formatMessage(message), e);
    }

    private static String formatMessage(String message, Object... args){
        return args != null && args.length > 0 ? String.format(message, args): message;
    }
}
