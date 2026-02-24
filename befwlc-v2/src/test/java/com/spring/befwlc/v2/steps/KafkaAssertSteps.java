package com.spring.befwlc.v2.steps;

import com.spring.befwlc.v2.await.MessageAwaiter;
import com.spring.befwlc.v2.context.ScenarioContext;
import com.spring.befwlc.v2.dsl.DslResolver;
import com.spring.befwlc.v2.kafka.KafkaConstants;
import com.spring.befwlc.v2.matching.MessageFilterSet;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class KafkaAssertSteps {

    private final MessageAwaiter messageAwaiter;
    private final DslResolver dslResolver;
    private final ScenarioContext scenarioContext;

    @Then("an event with the following fields is posted on {string} topic")
    public void assertMessagePosted(String topic, Map<String, String> filters) {
        MessageFilterSet filterSet = new MessageFilterSet(filters, dslResolver, scenarioContext);
        filterSet.addFilter(KafkaConstants.TOPIC, topic);
        messageAwaiter.awaitMatch(filterSet);
    }

    @Then("no event with the following fields is posted on {string} topic")
    public void assertMessageNotPosted(String topic, Map<String, String> filters) {
        MessageFilterSet filterSet = new MessageFilterSet(filters, dslResolver, scenarioContext);
        filterSet.addFilter(KafkaConstants.TOPIC, topic);
        messageAwaiter.awaitNoMatch(filterSet);
    }
}
