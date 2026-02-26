package com.tools.kafkabrowser.service;

import com.tools.kafkabrowser.config.KafkaProperties;
import com.tools.kafkabrowser.config.KafkaSslConfigurer;
import com.tools.kafkabrowser.model.TopicInfo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaAdminService {

    private final KafkaProperties kafkaProperties;
    private AdminClient adminClient;

    @PostConstruct
    void init() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 10_000);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 15_000);
        KafkaSslConfigurer.addKafkaSslProperties(props, kafkaProperties);
        adminClient = AdminClient.create(props);
        log.info("AdminClient created for {}", kafkaProperties.getBootstrapServers());
    }

    @PreDestroy
    void close() {
        if (adminClient != null) {
            adminClient.close();
        }
    }

    public List<TopicInfo> listTopics(boolean includeInternal) {
        try {
            ListTopicsOptions opts = new ListTopicsOptions().listInternal(includeInternal);
            Set<String> topicNames = adminClient.listTopics(opts).names().get(10, TimeUnit.SECONDS);

            Map<String, TopicDescription> descriptions = adminClient
                    .describeTopics(topicNames).allTopicNames().get(10, TimeUnit.SECONDS);

            return descriptions.values().stream()
                    .map(desc -> new TopicInfo(
                            desc.name(),
                            desc.partitions().size(),
                            desc.isInternal()))
                    .sorted(Comparator.comparing(TopicInfo::name))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to list topics", e);
            throw new RuntimeException("Failed to list topics: " + e.getMessage(), e);
        }
    }
}
