package com.spring.befwlc.steps;

import com.aventstack.extentreports.ExtentTest;
import com.spring.befwlc.configuration.ExtentReportUtil;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ScenarioHooks {

    private final ExtentReportUtil reportUtil;

    @Before
    public void setUp(Scenario scenario) {
        ExtentTest test = reportUtil.startTest(scenario.getName(),
                String.join(",", scenario.getSourceTagNames()));

        for (String tag : scenario.getSourceTagNames()) {
            test.assignCategory(tag.replace("@", ""));
        }
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            reportUtil.logFail("Scenario failed: " + scenario.getName());
        } else {
            reportUtil.logPass("Scenario passed: " + scenario.getName());
        }

        reportUtil.endTest();
    }
}
