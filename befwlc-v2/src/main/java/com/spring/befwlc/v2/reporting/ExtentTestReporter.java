package com.spring.befwlc.v2.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExtentTestReporter implements TestReporter {

    private final ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();

    @Override
    public void startTest(String name, Collection<String> tags) {
        ExtentTest test = extentReports.createTest(name, String.join(", ", tags));
        tags.forEach(tag -> test.assignCategory(tag.replace("@", "")));
        currentTest.set(test);
    }

    @Override
    public void logPass(String message) {
        ExtentTest test = currentTest.get();
        if (test != null) test.pass(message);
    }

    @Override
    public void logFail(String message) {
        ExtentTest test = currentTest.get();
        if (test != null) test.fail(message);
    }

    @Override
    public void logInfo(String message) {
        ExtentTest test = currentTest.get();
        if (test != null) test.info(message);
    }

    @Override
    public void logException(Throwable throwable) {
        ExtentTest test = currentTest.get();
        if (test != null) test.fail(throwable);
    }

    @Override
    public void endTest() {
        if (currentTest.get() != null) {
            extentReports.flush();
        }
    }
}
