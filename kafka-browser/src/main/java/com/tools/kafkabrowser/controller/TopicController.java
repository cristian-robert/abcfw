package com.tools.kafkabrowser.controller;

import com.tools.kafkabrowser.model.MessagePage;
import com.tools.kafkabrowser.model.TopicInfo;
import com.tools.kafkabrowser.service.KafkaAdminService;
import com.tools.kafkabrowser.service.KafkaReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TopicController {

    private final KafkaAdminService adminService;
    private final KafkaReaderService readerService;

    @GetMapping("/topics")
    public List<TopicInfo> listTopics(
            @RequestParam(defaultValue = "false") boolean includeInternal) {
        return adminService.listTopics(includeInternal);
    }

    @GetMapping("/topics/{topic}/messages")
    public MessagePage readMessages(
            @PathVariable String topic,
            @RequestParam(defaultValue = "-1") int partition,
            @RequestParam(defaultValue = "-1") long offset,
            @RequestParam(defaultValue = "25") int limit) {
        return readerService.readMessages(topic, partition, offset, Math.min(limit, 200));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
