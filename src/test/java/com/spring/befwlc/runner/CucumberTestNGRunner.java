package com.spring.befwlc.runner;

import com.spring.befwlc.BeFwLcApplication;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;

@Slf4j
@SpringBootTest(classes = {BeFwLcApplication.class})
@CucumberOptions(
        features = "classpath:features",
        glue = {"com.spring.befwlc.stepdefinitions", "com.spring.befwlc.configuration"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports.html",
                "json:target/cucumber.json"
        }
)
public class CucumberTestNGRunner extends AbstractTestNGCucumberTests {
    static {
        System.setProperty("cucumber.publish.quiet", "true");
    }


    @Override
    @DataProvider
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
