package com.spring.befwlc.configuration;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import org.springframework.stereotype.Component;

@Component
public class ExtentReportUtil {

    private final ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    public ExtentReportUtil(ExtentReports extentReports){
        this.extentReports = extentReports;
    }

    public ExtentTest startTest(String testName, String description){
        ExtentTest test = extentReports.createTest(testName, description);
        extentTest.set(test);
        return test;
    }

    public ExtentTest assignCategory(String... categories){
        return getTest().assignCategory(categories);
    }

    public ExtentTest getTest(){
        return extentTest.get();
    }

    public void logInfo(String message){
        getTest().info(message);
    }

    public void logPass(String message){
        getTest().pass(message);
    }

    public void logFail(String message){
        getTest().fail(message);
    }

    public void logException(Throwable message){
        getTest().fail(message);
    }

    public void endTest(){
        extentReports.flush();
    }
}
