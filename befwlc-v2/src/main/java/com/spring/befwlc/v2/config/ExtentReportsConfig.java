package com.spring.befwlc.v2.config;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExtentReportsConfig {

    @Value("${extent.reporter.spark.out:test-output/SparkReport/Spark.html}")
    private String reportPath;

    @Value("${extent.reporter.spark.theme:dark}")
    private String theme;

    @Value("${extent.reporter.spark.document-title:Automation Test Report}")
    private String documentTitle;

    @Value("${extent.reporter.spark.report-name:Test Execution Report}")
    private String reportName;

    @Bean
    public ExtentReports extentReports() {
        ExtentReports extent = new ExtentReports();
        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme("dark".equalsIgnoreCase(theme) ? Theme.DARK : Theme.STANDARD);
        spark.config().setDocumentTitle(documentTitle);
        spark.config().setReportName(reportName);
        extent.attachReporter(spark);
        return extent;
    }
}
