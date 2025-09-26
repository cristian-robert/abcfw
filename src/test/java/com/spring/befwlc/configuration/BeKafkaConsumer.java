package com.spring.befwlc.configuration;

import com.spring.befwlc.service.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.spring.befwlc.service.KafkaConstants.TEST_CONSUME_KAFKA_TOPIC;

@Component
public class BeKafkaConsumer extends KafkaConsumer {
    public BeKafkaConsumer() {
        super(TEST_CONSUME_KAFKA_TOPIC);
    }

    @KafkaListener(topics = {TEST_CONSUME_KAFKA_TOPIC})
    public void receiveRecord(final ConsumerRecord<byte[], byte[]> record){
        saveRecord(record);
    }
}
