package com.spring.befwlc.v2.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.befwlc.v2.context.ContextKey;
import com.spring.befwlc.v2.context.ScenarioContext;
import com.spring.befwlc.v2.kafka.KafkaProducerService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import test.prof.events.TransactionCreated;

import java.util.*;

@Slf4j
@CucumberContextConfiguration
@RequiredArgsConstructor
@SpringBootTest(classes = com.spring.befwlc.v2.BeFwLcTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class KafkaPublishSteps {

    private final KafkaProducerService producerService;
    private final ObjectMapper objectMapper;
    private final ScenarioContext scenarioContext;
    private final PayloadSteps payloadSteps;

    @When("{string} payload with the following details is posted on {string} endpoint")
    public void publishPayload(String jsonFile, String topic, DataTable dataTable) throws Exception {
        String benefId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        scenarioContext.put(ContextKey.BENEFICIARY_ID, benefId);

        String json = payloadSteps.loadJson(jsonFile);

        String txSeq = String.valueOf(new Random().nextInt(999999999 - 1000 + 1) + 1000);
        scenarioContext.put(ContextKey.TX_SEQ, txSeq);

        Map<String, String> modifications = new HashMap<>(dataTable.asMap(String.class, String.class));
        modifications.put("body.receivingPartyName", benefId);
        modifications.put("body.transactionIdentifierSequence", txSeq);

        for (Map.Entry<String, String> entry : modifications.entrySet()) {
            json = payloadSteps.updateField(json, entry.getKey(), entry.getValue());
        }

        scenarioContext.put(ContextKey.LAST_PAYLOAD, json);

        TransactionCreated transaction = objectMapper.readValue(json, TransactionCreated.class);
        producerService.sendMessage(transaction);
        log.info("Published message to topic '{}' with benefId={} txSeq={}", topic, benefId, txSeq);
    }
}
