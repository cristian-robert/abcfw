package com.spring.befwlc.v2.reporting;

import java.util.Set;

public interface TestReporter {
    void startTest(String name, Set<String> tags);
    void logPass(String message);
    void logFail(String message);
    void logInfo(String message);
    void logException(Throwable throwable);
    void endTest();
}
