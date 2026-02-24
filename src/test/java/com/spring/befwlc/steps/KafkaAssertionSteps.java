package com.spring.befwlc.steps;

import com.spring.befwlc.configuration.BeKafkaConsumer;
import com.spring.befwlc.context.ScenarioContext;
import com.spring.befwlc.entry_filter.EntryFilters;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class KafkaAssertionSteps {

    @Value("${amsAssert.check}")
    private Boolean checkAmsAssert;

    private final BeKafkaConsumer beKafkaConsumer;
    private final ScenarioContext scenarioContext;

    @Then("an event with the following fields is posted on {string} topic")
    public void findMatchingMessageInTopic(String topic, final Map<String, String> filters) {
        if (checkAmsAssert) {
            Map<String, String> amsFilters = new HashMap<>(filters);
            amsFilters.put("Message.Actions[6].ActionName", "BookingAccepted");
            final EntryFilters kafkaFiltersWithAms = new EntryFilters(filters, scenarioContext);
            beKafkaConsumer.assertKafkaMessageIsPosted(kafkaFiltersWithAms);
        } else {
            final EntryFilters kafkaFilters = new EntryFilters(filters, scenarioContext);
            beKafkaConsumer.assertKafkaMessageIsPosted(kafkaFilters);
        }
    }
}
