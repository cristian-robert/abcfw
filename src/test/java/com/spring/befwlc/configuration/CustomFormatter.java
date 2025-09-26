package com.spring.befwlc.configuration;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomFormatter implements ConcurrentEventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher){
        publisher.registerHandlerFor(TestStepStarted.class, this::handleTestStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
    }

    private void handleTestStepStarted(TestStepStarted event){
        if(event.getTestStep() instanceof PickleStepTestStep){
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
            log.info("\nExecuting: {} " , testStep.getStep().getText());
        }
    }

    private void handleTestStepFinished(TestStepFinished event){
        if(event.getResult().getStatus() != Status.PASSED){
            log.error("Step failed: {}", event.getResult().getError().toString());
        }
    }
}
