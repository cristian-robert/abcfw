package com.spring.billing.processor;

import com.spring.billing.service.FeeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import test.billing.events.BillingResponse;
import test.billing.events.InfoEntry;
import test.prof.events.TransactionCreated;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingProcessor {

    private static final String PRODUCT_ID = "31";
    private static final String FEE_PLAN_ID = "1";

    private final KafkaTemplate<String, BillingResponse> kafkaTemplate;

    @Value("${billing.topic.outbound}")
    private String outboundTopic;

    @KafkaListener(topics = "${billing.topic.inbound}", groupId = "${spring.kafka.consumer.group-id}")
    public void process(TransactionCreated transaction) {
        String amount = transaction.getBody().getAmount() != null
                ? transaction.getBody().getAmount().toString()
                : null;

        if (amount == null) {
            log.warn("Received transaction with null amount, skipping");
            return;
        }

        String fee = FeeCalculator.calculate(amount);
        log.info("Processing transaction: amount={} -> fee={}", amount, fee);

        BillingResponse response = BillingResponse.newBuilder()
                .setGeneralInformation(List.of(
                        InfoEntry.newBuilder().setValue(PRODUCT_ID).build(),
                        InfoEntry.newBuilder().setValue(FEE_PLAN_ID).build(),
                        InfoEntry.newBuilder().setValue(fee).build()
                ))
                .build();

        kafkaTemplate.send(outboundTopic, response);
        log.info("Sent billing response to {}: ProductId={}, FeePlanId={}, FeeAmount={}",
                outboundTopic, PRODUCT_ID, FEE_PLAN_ID, fee);
    }
}
