package com.tools.kafkabrowser.controller;

import com.tools.kafkabrowser.model.*;
import com.tools.kafkabrowser.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin
@RequiredArgsConstructor
public class ProducerController {

    private final KafkaProducerService producerService;

    @PostMapping("/produce")
    public ResponseEntity<ProduceResponse> produce(@RequestBody ProduceRequest request) {
        try {
            RecordMetadata metadata = producerService.send(
                    request.topic(), request.schemaSubject(), request.jsonContent());
            return ResponseEntity.ok(new ProduceResponse(
                    true, metadata.topic(), metadata.partition(), metadata.offset(), null));
        } catch (Exception e) {
            log.error("Failed to produce message to {}", request.topic(), e);
            return ResponseEntity.ok(new ProduceResponse(
                    false, request.topic(), -1, -1, e.getMessage()));
        }
    }

    @PostMapping("/produce/bulk")
    public ResponseEntity<BulkProduceResponse> produceBulk(@RequestBody BulkProduceRequest request) {
        List<ProduceResponse> results = new ArrayList<>();
        int succeeded = 0;
        int failed = 0;

        for (String jsonContent : request.messages()) {
            try {
                RecordMetadata metadata = producerService.send(
                        request.topic(), request.schemaSubject(), jsonContent);
                results.add(new ProduceResponse(
                        true, metadata.topic(), metadata.partition(), metadata.offset(), null));
                succeeded++;
            } catch (Exception e) {
                log.error("Failed to produce bulk message to {}", request.topic(), e);
                results.add(new ProduceResponse(
                        false, request.topic(), -1, -1, e.getMessage()));
                failed++;
            }
        }

        return ResponseEntity.ok(new BulkProduceResponse(
                request.messages().size(), succeeded, failed, results));
    }

    @GetMapping("/schemas/subjects")
    public ResponseEntity<List<String>> listSubjects() {
        try {
            return ResponseEntity.ok(producerService.listSubjects());
        } catch (Exception e) {
            log.error("Failed to list schema subjects", e);
            return ResponseEntity.ok(List.of());
        }
    }
}
