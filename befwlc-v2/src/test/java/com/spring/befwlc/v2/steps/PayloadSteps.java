package com.spring.befwlc.v2.steps;

import com.spring.befwlc.v2.payload.PayloadLoader;
import com.spring.befwlc.v2.payload.PayloadMutator;
import com.spring.befwlc.v2.reporting.AzureDevOpsReporter;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class PayloadSteps {

    private final PayloadLoader payloadLoader;
    private final PayloadMutator payloadMutator;

    @Autowired(required = false)
    private AzureDevOpsReporter azureReporter;

    @Given("I set testId for my current testCase")
    public void setTestId(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        String id = data.get("testCaseId");
        if (id == null) {
            id = data.get("testcaseId");
        }
        if (azureReporter != null && id != null) {
            azureReporter.setCurrentTestCaseId(id);
        }
    }

    public String loadJson(String jsonFile) {
        return payloadLoader.loadJson(jsonFile);
    }

    public String updateField(String json, String path, String value) {
        return payloadMutator.setValue(json, path, value);
    }
}
