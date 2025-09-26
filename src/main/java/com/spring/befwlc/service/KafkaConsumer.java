package com.spring.befwlc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.configuration.AwaitConfiguration;
import com.spring.befwlc.entry_filter.EntryFilters;
import com.spring.befwlc.entry_filter.EntryFinder;
import com.spring.befwlc.exceptions.TestExecutionException;
import com.spring.befwlc.handlers.AwaitHandler;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.spring.befwlc.service.KafkaConstants.*;

@Slf4j
public abstract class KafkaConsumer {

    protected final ObjectMapper mapper = new ObjectMapper();
    protected final List<ObjectNode> records = new CopyOnWriteArrayList<>();
    private final String[] messageUniqueKeys;
    private final String topicName;

    @Autowired(required = false)
    protected KafkaAvroDeserializer kafkaAvroDeserializer;

    @Autowired(required = false)
    @Qualifier("kafkaAwaitHandlerConfiguration")
    private AwaitConfiguration awaitConfiguration;

    protected KafkaConsumer(String topicName, String... messageUniqueKeys) {
        this.topicName = topicName;
        this.messageUniqueKeys = messageUniqueKeys;
    }

    public abstract void receiveRecord(final ConsumerRecord<byte[], byte[]> record);

    public void assertKafkaMessageIsPosted(final EntryFilters entryFilters){
        checkKafkaConfiguration();

        entryFilters.addFilter(TOPIC, topicName);

        log.info("Assert message is posted on Kafka '{}' topic. Filters: {}", topicName, entryFilters.toString());

        try{
            AwaitHandler.awaitTrue(() -> EntryFinder.entryFoundByFilters(records, entryFilters), awaitConfiguration);
        } catch (Exception e) {
            EntryFinder.logPartiallyMatchedEntries(records, entryFilters.getPartiallyMatchedEntriesWithMatchedKeys(messageUniqueKeys), true);
            throw new TestExecutionException("No Kafka entry found by given filters");
        }
    }

    public void assertKafkaMessageIsNotPosted(final EntryFilters entryFilters){
        entryFilters.addFilter(TOPIC, topicName);
        log.info("Assert message is not posted on Kafka '{}' topic. Filters: {}", topicName, entryFilters.toString());

        try {
            AwaitHandler.awaitTrue(() -> EntryFinder.entryNotFoundByFilters(records, entryFilters), awaitConfiguration);
        } catch (Exception e){
            log.info("No Kafka entry fully matched the given filters");
        }
    }

    protected void logError(final Exception e){
        log.error("Kafka message deserialization failed, reason: ", e);
    }

    protected void addHeadersToRecord(ObjectNode decodedMessage, ConsumerRecord<byte[], byte[]> record){
        ObjectNode headers = mapper.createObjectNode();
        record.headers().forEach(header -> {
            final byte[] headerBites = header.value();
            final String headerValue = Objects.nonNull(headerBites) ? new String(headerBites, StandardCharsets.UTF_8) : null;
        });
        decodedMessage.set(HEADERS, headers);
    }

    protected synchronized void addRecord(ObjectNode message){
        records.add(message);
    }

    protected synchronized void saveRecord(final ConsumerRecord<byte[], byte[]> record){
        try{
            final ObjectNode decodedMessage = mapper.createObjectNode();
            decodedMessage.put(TOPIC, record.topic());
            decodedMessage.put(PARTITION, record.partition());
            decodedMessage.put(OFFSET, record.offset());
            decodedMessage.put(TIMESTAMP, record.timestamp());
            decodedMessage.put(MESSAGE, mapper.readTree(kafkaAvroDeserializer.deserialize(record.topic(), record.value()).toString()));
            addHeadersToRecord(decodedMessage, record);
            addRecord(decodedMessage);
        } catch (final Exception e){
            logError(e);
        }
    }

    private void checkKafkaConfiguration() {
        if (awaitConfiguration == null && kafkaAvroDeserializer == null) {
            throw new TestExecutionException("Kafka configuration is not enabled. Use 'P' or 'S' to enable Kafka configuration.");
        }
    }
}
