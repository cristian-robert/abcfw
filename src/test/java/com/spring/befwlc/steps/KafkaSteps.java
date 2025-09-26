package com.spring.befwlc.steps;


import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.spring.befwlc.TestConfig;
import com.spring.befwlc.configuration.AzureTestReporter;
import com.spring.befwlc.configuration.BeKafkaConsumer;
import com.spring.befwlc.configuration.ExtentReportUtil;
import com.spring.befwlc.configuration.KafkaProducerConfig;
import com.spring.befwlc.entry_filter.EntryFilters;
import com.spring.befwlc.service.KafkaProducerService;
import com.spring.befwlc.utils.TestContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.FileCopyUtils;
import test.prof.events.TransactionCreated;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

@CucumberContextConfiguration
@ComponentScan(basePackages = {"com.spring.befwlc"})
@RequiredArgsConstructor
@Slf4j
@SpringBootTest(classes = TestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
public class KafkaSteps {

    @Value("${amsAssert.check}")
    private Boolean checkAmsAssert;

    @Autowired
    private ExtentReportUtil reportUtil;

    @Autowired
    private AzureTestReporter azureTestReporter;

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

    @Autowired
    private final KafkaProducerService producerService;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final TestContext testContext;

    @Autowired
    private BeKafkaConsumer beKafkaConsumer;

    @When("{string} payload with the following details is posted on {string} endpoint")
    public void sendJsonToKafkaTopic(String jsonFile, String topic, DataTable dataTable) throws Exception {
        String benefId = testContext.generateShortUUID();
        testContext.set("beneficiaryId", benefId);
        String jsonString = loadJsonFromPayloadFile(jsonFile);
        String txSeq = generateRandomTransactionIdentifier();
        Map<String, String> modifications = new HashMap<>(dataTable.asMap(String.class, String.class));
        modifications.put("body.receivingPartyName", testContext.get("beneficiaryId").toString());
        modifications.put("body.transactionIdentifierSequence", txSeq);
        testContext.set("txSeq", txSeq);
        for (Map.Entry<String, String> entry : modifications.entrySet()) {
            jsonString = updateJsonValue(jsonString, entry.getKey(), entry.getValue());
        }

        TransactionCreated transaction = objectMapper.readValue(jsonString, TransactionCreated.class);
        if (Objects.equals(topic, "TEST_CONSUME_TOPIC")) {
            producerService.sendMessage(transaction);
        }
    }


    @Then("an event with the following fields is posted on {string} topic")
    public void findMatchingMessageInTopic(String topic, final Map<String, String> filters) {
        if (checkAmsAssert) {
            Map<String, String> amsFilters = new HashMap<>(filters);
            amsFilters.put("Message.Actions[6].ActionName", "BookingAccepted");
            final EntryFilters kafkaFiltersWithAms = new EntryFilters(filters);
            beKafkaConsumer.assertKafkaMessageIsPosted(kafkaFiltersWithAms);
        } else {
            final EntryFilters kafkaFilters = new EntryFilters(filters);
            beKafkaConsumer.assertKafkaMessageIsPosted(kafkaFilters);
        }
    }

    private String loadJsonFromPayloadFile(String jsonFile) throws Exception {
        String filePath = jsonFile.startsWith("/") ? jsonFile :"/payload/" + jsonFile;
        Resource resource = resourceLoader.getResource("classpath: " + filePath);

        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            String content = FileCopyUtils.copyToString(reader);
            //Validate that it`s a valid JSON
            objectMapper.readTree(content);
            return content;
        } catch (Exception e) {
            log.error("Error loading JSON file {}: {}", filePath, e.getMessage());
            throw new RuntimeException("Error loading JSON payload: " + filePath, e);
        }
    }

    private String updateJsonValue(String jsonString, String path, String value) {
        try {
            // Handle null or "null" string values

            if (value == null || value.equalsIgnoreCase("null")) {
                return JsonPath.parse(jsonString)
                        .set(normalizePath(path), null)
                        .jsonString();
            }

            if (value.isEmpty()) {
                return JsonPath.parse(jsonString)
                        .set(normalizePath(path), "")
                        .jsonString();
            }

            if (value.matches("-?\\d+(\\.\\d+)?")) {
                System.out.println("Raw value: " + value);
                if (value.contains(".")) {
                    return JsonPath.parse(jsonString)
                            .set(normalizePath(path), Double.parseDouble(value))
                            .jsonString();
                } else {
                    return JsonPath.parse(jsonString)
                            .set(normalizePath(path), Long.parseLong(value))
                            .jsonString();
                }
            }

            // Handle boolean values
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return JsonPath.parse(jsonString)
                        .set(normalizePath(path), Boolean.parseBoolean(value))
                        .jsonString();
            }

            // Handle special string values that might indicate null
            if(value.equalsIgnoreCase("<null>") || value.equalsIgnoreCase("${null}")){
                return JsonPath.parse(jsonString)
                        .set(normalizePath(path), null)
                        .jsonString();
            }

            return JsonPath.parse(jsonString)
                    .set(normalizePath(path), value)
                    .jsonString();
        } catch (PathNotFoundException e) {
            log.error("Path not found: {}", path);
            throw new RuntimeException("Invalid JSON path: " + path, e);
        } catch (Exception e){
            log.error("Error updating JSON value for path: {}, value: {}", path, value);
            throw new RuntimeException("Error updating JSON value: " + e.getMessage(), e);
        }
    }

    private String normalizePath(String path){
        return path.startsWith("$") ? path :"$." + path;
    }

    @Given("I set testId for my current testCase")
    public void iSetTestIdForMyCurrentTestCase(DataTable dataTable){
        Map<String, String> data = new HashMap<>(dataTable.asMap(String.class, String.class));

        String id = data.get("testCaseId");
//      azureTestReporter.setCurrentTestCaseId(id);
    }

    private String generateRandomTransactionIdentifier(){
        int min = 1000;
        int max = 999999999;

        return String.valueOf(new Random().nextInt(max - min + 1) + min);
    }

}
