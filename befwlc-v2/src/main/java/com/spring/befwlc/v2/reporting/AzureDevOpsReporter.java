package com.spring.befwlc.v2.reporting;

import com.spring.befwlc.v2.config.AzureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
@ConditionalOnProperty(name = "azure.update-results", havingValue = "true")
@RequiredArgsConstructor
public class AzureDevOpsReporter implements TestReporter {

    private final AzureProperties azureProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    private String currentTestCaseId;
    private final Map<String, List<Boolean>> testResults = new HashMap<>();
    private boolean currentScenarioFailed = false;

    @Override
    public void startTest(String name, Set<String> tags) {
        currentScenarioFailed = false;
    }

    @Override
    public void logPass(String message) {
        currentScenarioFailed = false;
    }

    @Override
    public void logFail(String message) {
        currentScenarioFailed = true;
    }

    @Override
    public void logInfo(String message) {}

    @Override
    public void logException(Throwable throwable) {
        currentScenarioFailed = true;
    }

    @Override
    public void endTest() {
        if (currentTestCaseId == null) return;

        testResults.putIfAbsent(currentTestCaseId, new ArrayList<>());
        testResults.get(currentTestCaseId).add(currentScenarioFailed);
    }

    public void setCurrentTestCaseId(String testCaseId) {
        this.currentTestCaseId = testCaseId;
    }

    public void flushResults(String testCaseId) {
        List<Boolean> results = testResults.remove(testCaseId);
        if (results == null || results.isEmpty()) return;

        // Fixed: allPassed = none of the scenarios failed
        boolean allPassed = results.stream().noneMatch(failed -> failed);
        String outcome = allPassed ? "Passed" : "Failed";

        try {
            updateTestResult(testCaseId, outcome, "Automated test: " + outcome);
        } catch (Exception e) {
            log.error("Failed to update Azure DevOps test result for testCaseId={}: {}", testCaseId, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void updateTestResult(String testCaseId, String outcome, String comment) {
        String baseUrl = String.format("https://dev.azure.com/%s/%s/_apis",
                azureProperties.getOrganization(), azureProperties.getProject());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " +
                Base64.getEncoder().encodeToString((":" + azureProperties.getPat()).getBytes()));

        // Get test points
        String pointsUrl = String.format("%s/test/Plans/%s/Suites/%s/points?api-version=7.1",
                baseUrl, azureProperties.getTestPlanId(), azureProperties.getSuiteId());

        ResponseEntity<Map> pointsResponse = restTemplate.exchange(
                pointsUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        List<Map<String, Object>> points = (List<Map<String, Object>>) pointsResponse.getBody().get("value");
        Integer pointId = points.stream()
                .filter(p -> testCaseId.equals(String.valueOf(((Map<?, ?>) p.get("testCase")).get("id"))))
                .findFirst()
                .map(p -> (Integer) p.get("id"))
                .orElseThrow(() -> new RuntimeException("Test point not found for testCaseId: " + testCaseId));

        // Create run
        String runUrl = baseUrl + "/test/runs?api-version=7.1";
        Map<String, Object> runData = Map.of(
                "name", comment,
                "plan", Map.of("id", azureProperties.getTestPlanId()),
                "state", "InProgress");

        ResponseEntity<Map> runResponse = restTemplate.exchange(
                runUrl, HttpMethod.POST, new HttpEntity<>(runData, headers), Map.class);
        Integer runId = (Integer) runResponse.getBody().get("id");

        // Add result
        String resultUrl = String.format("%s/test/runs/%d/results?api-version=7.1", baseUrl, runId);
        Map<String, Object> result = Map.of(
                "testPoint", Map.of("id", pointId),
                "testCase", Map.of("id", testCaseId),
                "testCaseRevision", 1,
                "testCaseTitle", comment,
                "outcome", outcome,
                "state", "Completed");

        restTemplate.exchange(resultUrl, HttpMethod.POST,
                new HttpEntity<>(Collections.singletonList(result), headers), Map.class);

        // Complete run
        String completeUrl = String.format("%s/test/runs/%d?api-version=7.1", baseUrl, runId);
        HttpHeaders patchHeaders = new HttpHeaders();
        patchHeaders.putAll(headers);
        patchHeaders.set("X-HTTP-Method-Override", "PATCH");

        restTemplate.exchange(completeUrl, HttpMethod.POST,
                new HttpEntity<>(Map.of("state", "Completed"), patchHeaders), Map.class);

        log.info("Azure DevOps: Updated testCaseId={} with outcome={}", testCaseId, outcome);
    }
}
