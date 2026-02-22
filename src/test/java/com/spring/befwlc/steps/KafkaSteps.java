package com.spring.befwlc.steps;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.befwlc.TestConfig;
import com.spring.befwlc.context.ScenarioContext;
import com.spring.befwlc.context.ScenarioContextKeys;
import com.spring.befwlc.service.KafkaProducerService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import test.prof.events.TransactionCreated;

import java.util.*;

@CucumberContextConfiguration
@ComponentScan(basePackages = {"com.spring.befwlc"})
@RequiredArgsConstructor
@Slf4j
@SpringBootTest(classes = TestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
public class KafkaSteps {

    private final KafkaProducerService producerService;
    private final ObjectMapper objectMapper;
    private final ScenarioContext scenarioContext;
    private final PayloadSteps payloadSteps;

    @When("{string} payload with the following details is posted on {string} endpoint")
    public void sendJsonToKafkaTopic(String jsonFile, String topic, DataTable dataTable) throws Exception {
        String benefId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        scenarioContext.put(ScenarioContextKeys.BENEFICIARY_ID, benefId);
        String jsonString = payloadSteps.loadJsonFromPayloadFile(jsonFile);
        String txSeq = generateRandomTransactionIdentifier();
        Map<String, String> modifications = new HashMap<>(dataTable.asMap(String.class, String.class));
        modifications.put("body.receivingPartyName", benefId);
        modifications.put("body.transactionIdentifierSequence", txSeq);
        scenarioContext.put(ScenarioContextKeys.TX_SEQ, txSeq);
        for (Map.Entry<String, String> entry : modifications.entrySet()) {
            jsonString = payloadSteps.updateJsonValue(jsonString, entry.getKey(), entry.getValue());
        }

        TransactionCreated transaction = objectMapper.readValue(jsonString, TransactionCreated.class);
        if (Objects.equals(topic, "TEST_CONSUME_TOPIC")) {
            producerService.sendMessage(transaction);
        }
    }

    private String generateRandomTransactionIdentifier() {
        int min = 1000;
        int max = 999999999;

        return String.valueOf(new Random().nextInt(max - min + 1) + min);
    }
}
