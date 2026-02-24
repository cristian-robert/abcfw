package com.spring.befwlc.v2.steps;

import com.spring.befwlc.v2.kafka.KafkaListenerReadyGuard;
import com.spring.befwlc.v2.kafka.KafkaMessageStore;
import com.spring.befwlc.v2.reporting.TestReporter;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ScenarioHooks {

    private final List<TestReporter> reporters;
    private final KafkaListenerReadyGuard listenerReadyGuard;
    private final KafkaMessageStore messageStore;

    @Before(order = 0)
    public void waitForKafka() {
        listenerReadyGuard.waitForListeners();
        messageStore.clear();
    }

    @Before(order = 1)
    public void startReporting(Scenario scenario) {
        Collection<String> tags = scenario.getSourceTagNames();
        reporters.forEach(r -> r.startTest(scenario.getName(), tags));
    }

    @After
    public void endReporting(Scenario scenario) {
        if (scenario.isFailed()) {
            reporters.forEach(r -> r.logFail("Scenario failed: " + scenario.getName()));
        } else {
            reporters.forEach(r -> r.logPass("Scenario passed: " + scenario.getName()));
        }
        reporters.forEach(TestReporter::endTest);
    }
}
