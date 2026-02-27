package com.tools.kafkabrowser.controller;

import com.tools.kafkabrowser.model.*;
import com.tools.kafkabrowser.repository.AppSettingRepository;
import com.tools.kafkabrowser.repository.CollectionRepository;
import com.tools.kafkabrowser.repository.TemplateRepository;
import com.tools.kafkabrowser.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/collections")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class CollectionController {
    private final CollectionRepository collectionRepository;
    private final TemplateRepository templateRepository;
    private final KafkaProducerService kafkaProducerService;
    private final AppSettingRepository appSettingRepository;

    @GetMapping
    public List<CollectionSummaryDto> listCollections() {
        return collectionRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(c -> CollectionSummaryDto.from(c, templateRepository.countByCollectionId(c.getId())))
                .toList();
    }

    @GetMapping("/{id}")
    public Collection getCollection(@PathVariable Long id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
    }

    @PostMapping
    public ResponseEntity<Collection> createCollection(@RequestBody CollectionDto dto) {
        Collection collection = new Collection();
        collection.setName(dto.name());
        collection.setTopicName(dto.topicName());
        collection.setSchemaSubject(dto.schemaSubject());
        collection.setAvscContent(dto.avscContent());
        Collection saved = collectionRepository.save(collection);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Collection updateCollection(@PathVariable Long id, @RequestBody CollectionDto dto) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
        collection.setName(dto.name());
        collection.setTopicName(dto.topicName());
        collection.setSchemaSubject(dto.schemaSubject());
        collection.setAvscContent(dto.avscContent());
        return collectionRepository.save(collection);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        if (!collectionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found");
        }
        List<Template> templates = templateRepository.findByCollectionIdOrderByUpdatedAtDesc(id);
        templateRepository.deleteAll(templates);
        collectionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<BulkProduceResponse> runCollection(@PathVariable Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));

        String topic = resolveOrGlobal(collection.getTopicName(), "global.topic");
        String schema = resolveOrGlobal(collection.getSchemaSubject(), "global.schemaSubject");

        if (topic == null || topic.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No topic configured (collection or global)");
        }
        List<Template> templates = templateRepository.findByCollectionIdOrderByUpdatedAtDesc(id);
        if (templates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection has no templates");
        }
        List<ProduceResponse> results = new ArrayList<>();
        int succeeded = 0;
        int failed = 0;
        for (Template template : templates) {
            try {
                var metadata = kafkaProducerService.send(topic, schema, template.getJsonContent());
                results.add(new ProduceResponse(true, metadata.topic(), metadata.partition(), metadata.offset(), null));
                succeeded++;
            } catch (Exception e) {
                log.error("Failed to produce template '{}': {}", template.getName(), e.getMessage());
                results.add(new ProduceResponse(false, topic, -1, -1, e.getMessage()));
                failed++;
            }
        }
        return ResponseEntity.ok(new BulkProduceResponse(templates.size(), succeeded, failed, results));
    }

    private String resolveOrGlobal(String value, String globalKey) {
        if (value != null && !value.isBlank()) return value;
        return appSettingRepository.findById(globalKey)
                .map(AppSetting::getValue)
                .filter(v -> !v.isBlank())
                .orElse(null);
    }
}
