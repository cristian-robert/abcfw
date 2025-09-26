package com.spring.befwlc.configuration;

import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Scope("cucumber-glue")
public class AzureTestReporter {
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, List<Boolean>> testResults = new HashMap<>();
    private final Map<String, String> scenarioIdNames = new HashMap<>();
    private String lastProcessedScenarioId = null;

    private String testCaseId; // 10 usages

    @Value("${azure.pat}")
    private String pat;

    @Value("${azure.organization}")
    private String organization;

    @Value("${azure.project}")
    private String project;

    @Value("${azure.testPlanId}")
    private String testPlanId;

    @Value("${azure.suiteId}")
    private String suiteId;
    @Value("${azure.updateResults}")
    private boolean updateResults;

    public void setCurrentTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    @After // no usages ▲ cristian robert losef
    public void afterScenario(Scenario scenario) {
        if (testCaseId != null && updateResults) {
            testResults.putIfAbsent(testCaseId, new ArrayList<>());
            testResults.get(testCaseId).add(scenario.isFailed());

            String currentBase = getScenarioIdBase(scenario.getId());
            if (!currentBase.equals(getScenarioIdBase(getNextScenarioId(scenario)))) {
                boolean allPassed = testResults.get(testCaseId).stream().allMatch(result -> result);
                updateTestResult(testCaseId, allPassed ? "Passed" : "Failed",
                        scenarioIdNames.get(testCaseId) + "\nExample results: " +
                                testResults.get(testCaseId).toString());
            }
            testResults.remove(testCaseId);
            scenarioIdNames.remove(testCaseId);
        }
    }

    private String getScenarioIdBase(String id) { // 2 usages ▲ cristian robert losef
        int lastSemicolon = id.lastIndexOf(":");
        return lastSemicolon != -1 ? id.substring(0, lastSemicolon) : id;
    }

    private String getNextScenarioId(Scenario scenario) {
        return scenario.getId() + ":" + (scenario.getLine() + 1);
    }

    private void updateTestResult(String testCaseId, String outcome, String comment) { // 1 usage ▲ cristian robert losef
        String pointsUrl = String.format("https://dev.azure.com/%s/%s/_apis/test/Plans/%s/Suites/%s/points?api-version=7.1",
                organization, project, testPlanId, suiteId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((":" + pat).getBytes()));

        HttpEntity<Void> pointsRequest = new HttpEntity<>(headers);
        ResponseEntity<Map> pointsResponse = restTemplate.exchange(pointsUrl, HttpMethod.GET, pointsRequest, Map.class);
        List<Map<String, Object>> points = (List<Map<String, Object>>) pointsResponse.getBody().get("value");
        Integer pointId = points.stream()
                .filter(p -> testCaseId.equals(((Map) p.get("testCase")).get("id")))
                .findFirst()
                .map(p -> (Integer) p.get("id"))
                .orElseThrow(() -> new RuntimeException("Test point not found"));

        // Create run
        String runUrl = String.format("https://dev.azure.com/%s/%s/_apis/test/runs?api-version=7.1",
                organization, project);

        Map<String, Object> runData = new HashMap<>();
        runData.put("name", comment);
        runData.put("plan", Map.of("id", testPlanId));
        runData.put("state", "InProgress");

        ResponseEntity<Map> runResponse = restTemplate.exchange(runUrl, HttpMethod.POST, new HttpEntity<>(runData, headers), Map.class);
        System.out.println(runResponse.getBody());
        Integer runId = (Integer) runResponse.getBody().get("id");

        // Add result
        String resultUrl = String.format("https://dev.azure.com/%s/%s/_apis/test/runs/%d/results?api-version=7.1",
                organization, project, runId);

        Map<String, Object> result = new HashMap<>();
        result.put("testPoint", Map.of("id", pointId));
        result.put("testCase", Map.of("id", testCaseId));
        result.put("testCaseRevision", 1);
        result.put("testCaseTitle", comment);
        result.put("outcome", outcome);
        result.put("state", "Completed");

        HttpEntity<List<Map<String, Object>>> resultRequest = new HttpEntity<>(Collections.singletonList(result), headers);
        restTemplate.exchange(resultUrl, HttpMethod.POST, resultRequest, Map.class);

        // Complete run
        String completeUrl = String.format("https://dev.azure.com/%s/%s/_apis/test/runs/%d?api-version=7.1",
                organization, project, runId);

        Map<String, Object> completeData = new HashMap<>();
        completeData.put("state", "Completed");
        headers.setContentType(MediaType.valueOf("application/json"));
        headers.set("X-HTTP-Method-Override", "PATCH");
        HttpEntity<Map<String, Object>> completeRequest = new HttpEntity<>(completeData, headers);
        restTemplate.exchange(completeUrl, HttpMethod.POST, completeRequest, Map.class);
    }
}