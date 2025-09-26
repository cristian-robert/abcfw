package com.spring.befwlc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import test.prof.events.TransactionCreated;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<String, TransactionCreated> kafkaTemplate;

    public void sendMessage(TransactionCreated message){
        kafkaTemplate.sendDefault(message);
    }
}
