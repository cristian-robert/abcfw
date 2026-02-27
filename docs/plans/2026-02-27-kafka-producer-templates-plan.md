# Kafka Producer & Templates Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Extend kafka-browser with a producer UI — users upload/edit/save JSON templates and send them as Avro messages to Kafka topics.

**Architecture:** Add JPA + H2 to the existing kafka-browser Spring Boot backend for template persistence. Extract SchemaRegistryClient into a shared bean. Add a KafkaProducerService that converts JSON to Avro GenericRecords. Frontend gets a new "Templates" page with editable JSON tree/raw editor, template list sidebar, and single/bulk send capabilities.

**Tech Stack:** Java 17, Spring Boot 3.5.6, Spring Data JPA, H2, Confluent KafkaAvroSerializer, Next.js 16, React 19, shadcn/ui, Tailwind CSS 4

**Worktree:** `/Users/cristian-robertiosef/Desktop/Dev/abcfw/.worktrees/kafka-producer-templates/`
**Branch:** `feature/kafka-producer-templates`

---

## Phase 1: Backend Infrastructure

### Task 1: Add JPA + H2 dependencies

**Files:**
- Modify: `kafka-browser/pom.xml`

**Step 1: Add Spring Data JPA and H2 dependencies to pom.xml**

Add these two dependencies inside the `<dependencies>` block, after the existing Lombok dependency:

```xml
<!-- JPA + H2 for template persistence -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Step 2: Verify it compiles**

Run from project root: `./mvnw -pl kafka-browser compile -q`
Expected: no errors

**Step 3: Commit**

```
feat(kafka-browser): add JPA and H2 dependencies for template storage
```

---

### Task 2: Configure H2 database in application.yml

**Files:**
- Modify: `kafka-browser/src/main/resources/application.yml`

**Step 1: Add Spring datasource and JPA config**

Add the following block at the top level of application.yml (after the `server:` block, before `kafka:`):

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/kafka-browser-db
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  h2:
    console:
      enabled: true
      path: /h2-console
```

Also add `data/` to the project's `.gitignore` if not present (to exclude the H2 database files).

**Step 2: Verify it compiles**

Run: `./mvnw -pl kafka-browser compile -q`
Expected: no errors

**Step 3: Commit**

```
feat(kafka-browser): configure H2 file-based database for template persistence
```

---

### Task 3: Extract SchemaRegistryClient into shared config bean

**Files:**
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/config/SchemaRegistryConfig.java`
- Modify: `kafka-browser/src/main/java/com/tools/kafkabrowser/service/KafkaReaderService.java`

**Step 1: Create SchemaRegistryConfig**

```java
package com.tools.kafkabrowser.config;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SchemaRegistryConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public SchemaRegistryClient schemaRegistryClient() {
        Map<String, Object> srConfig = KafkaSslConfigurer.buildSchemaRegistrySslConfig(kafkaProperties);
        srConfig.put("schema.registry.url", kafkaProperties.getSchemaRegistryUrl());
        SchemaRegistryClient client = new CachedSchemaRegistryClient(
                List.of(kafkaProperties.getSchemaRegistryUrl()), 1000, srConfig);
        log.info("Schema Registry client created for {}", kafkaProperties.getSchemaRegistryUrl());
        return client;
    }
}
```

**Step 2: Modify KafkaReaderService to inject the shared bean**

Remove the inline SchemaRegistryClient creation from the constructor. Change the constructor to accept `SchemaRegistryClient` as a parameter:

Replace the existing constructor (lines 45-54) with:

```java
public KafkaReaderService(KafkaProperties kafkaProperties, ObjectMapper objectMapper,
                          SchemaRegistryClient schemaRegistryClient) {
    this.kafkaProperties = kafkaProperties;
    this.objectMapper = objectMapper;
    this.schemaRegistryClient = schemaRegistryClient;
}
```

**Step 3: Verify it compiles**

Run: `./mvnw -pl kafka-browser compile -q`
Expected: no errors

**Step 4: Commit**

```
refactor(kafka-browser): extract SchemaRegistryClient into shared config bean
```

---

## Phase 2: Template CRUD

### Task 4: Create Template entity

**Files:**
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/model/Template.java`

**Step 1: Create the entity class**

```java
package com.tools.kafkabrowser.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "templates")
@Getter
@Setter
@NoArgsConstructor
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String topicName;

    private String schemaSubject;

    @Column(columnDefinition = "CLOB")
    private String jsonContent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Step 2: Verify it compiles**

Run: `./mvnw -pl kafka-browser compile -q`
Expected: no errors

**Step 3: Commit**

```
feat(kafka-browser): add Template JPA entity for template storage
```

---

### Task 5: Create TemplateRepository

**Files:**
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/repository/TemplateRepository.java`

**Step 1: Create the repository interface**

```java
package com.tools.kafkabrowser.repository;

import com.tools.kafkabrowser.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    List<Template> findAllByOrderByUpdatedAtDesc();
}
```

**Step 2: Verify it compiles**

Run: `./mvnw -pl kafka-browser compile -q`
Expected: no errors

**Step 3: Commit**

```
feat(kafka-browser): add TemplateRepository for template CRUD operations
```

---

### Task 6: Create TemplateController with CRUD endpoints

**Files:**
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/controller/TemplateController.java`
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/model/TemplateDto.java`
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/model/TemplateSummaryDto.java`

**Step 1: Create TemplateSummaryDto (for list responses without full JSON content)**

```java
package com.tools.kafkabrowser.model;

import java.time.LocalDateTime;

public record TemplateSummaryDto(
        Long id,
        String name,
        String topicName,
        String schemaSubject,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TemplateSummaryDto from(Template template) {
        return new TemplateSummaryDto(
                template.getId(),
                template.getName(),
                template.getTopicName(),
                template.getSchemaSubject(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
```

**Step 2: Create TemplateDto (request body for create/update)**

```java
package com.tools.kafkabrowser.model;

public record TemplateDto(
        String name,
        String topicName,
        String schemaSubject,
        String jsonContent
) {}
```

**Step 3: Create TemplateController**

```java
package com.tools.kafkabrowser.controller;

import com.tools.kafkabrowser.model.Template;
import com.tools.kafkabrowser.model.TemplateDto;
import com.tools.kafkabrowser.model.TemplateSummaryDto;
import com.tools.kafkabrowser.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateRepository templateRepository;

    @GetMapping
    public List<TemplateSummaryDto> listTemplates() {
        return templateRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(TemplateSummaryDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public Template getTemplate(@PathVariable Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));
    }

    @PostMapping
    public ResponseEntity<Template> createTemplate(@RequestBody TemplateDto dto) {
        Template template = new Template();
        template.setName(dto.name());
        template.setTopicName(dto.topicName());
        template.setSchemaSubject(dto.schemaSubject());
        template.setJsonContent(dto.jsonContent());
        Template saved = templateRepository.save(template);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Template updateTemplate(@PathVariable Long id, @RequestBody TemplateDto dto) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));
        template.setName(dto.name());
        template.setTopicName(dto.topicName());
        template.setSchemaSubject(dto.schemaSubject());
        template.setJsonContent(dto.jsonContent());
        return templateRepository.save(template);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        if (!templateRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found");
        }
        templateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Step 4: Verify it compiles**

Run: `./mvnw -pl kafka-browser compile -q`
Expected: no errors

**Step 5: Commit**

```
feat(kafka-browser): add TemplateController with CRUD REST endpoints
```

---

## Phase 3: Kafka Producer

### Task 7: Create KafkaProducerService

**Files:**
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/service/KafkaProducerService.java`

**Step 1: Create the producer service**

This service handles JSON → Avro conversion and producing to Kafka. The key method is `jsonToGenericRecord` which is the reverse of the existing `avroToJson` in KafkaReaderService.

```java
package com.tools.kafkabrowser.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.kafkabrowser.config.KafkaProperties;
import com.tools.kafkabrowser.config.KafkaSslConfigurer;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;
    private final SchemaRegistryClient schemaRegistryClient;
    private final KafkaProducer<String, Object> producer;

    public KafkaProducerService(KafkaProperties kafkaProperties, ObjectMapper objectMapper,
                                SchemaRegistryClient schemaRegistryClient) {
        this.kafkaProperties = kafkaProperties;
        this.objectMapper = objectMapper;
        this.schemaRegistryClient = schemaRegistryClient;
        this.producer = createProducer();
    }

    public RecordMetadata send(String topic, String schemaSubject, String jsonContent) throws Exception {
        Object value;
        if (schemaSubject != null && !schemaSubject.isBlank()) {
            value = convertToAvro(schemaSubject, jsonContent);
        } else {
            value = jsonContent;
        }

        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, value);
        Future<RecordMetadata> future = producer.send(record);
        RecordMetadata metadata = future.get(30, TimeUnit.SECONDS);
        log.info("Produced message to topic={} partition={} offset={}", metadata.topic(), metadata.partition(), metadata.offset());
        return metadata;
    }

    public List<String> listSubjects() throws Exception {
        Collection<String> subjects = schemaRegistryClient.getAllSubjects();
        return subjects.stream().sorted().toList();
    }

    private GenericRecord convertToAvro(String schemaSubject, String jsonContent) throws Exception {
        var schemaMetadata = schemaRegistryClient.getLatestSchemaMetadata(schemaSubject);
        String schemaString = schemaMetadata.getSchema();
        Schema avroSchema = new Schema.Parser().parse(schemaString);
        log.debug("Using schema {} (id={}) for subject {}", avroSchema.getFullName(), schemaMetadata.getId(), schemaSubject);

        JsonNode jsonNode = objectMapper.readTree(jsonContent);
        return jsonToGenericRecord(jsonNode, avroSchema);
    }

    private GenericRecord jsonToGenericRecord(JsonNode jsonNode, Schema schema) {
        GenericRecord record = new GenericData.Record(schema);
        for (Schema.Field field : schema.getFields()) {
            JsonNode fieldValue = jsonNode.get(field.name());
            if (fieldValue == null || fieldValue.isNull()) {
                record.put(field.name(), null);
            } else {
                record.put(field.name(), convertJsonToAvro(fieldValue, field.schema()));
            }
        }
        return record;
    }

    private Object convertJsonToAvro(JsonNode jsonNode, Schema schema) {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        }

        Schema effectiveSchema = schema;
        if (schema.getType() == Schema.Type.UNION) {
            for (Schema branch : schema.getTypes()) {
                if (branch.getType() == Schema.Type.NULL) continue;
                effectiveSchema = branch;
                break;
            }
            if (jsonNode.isNull()) return null;
        }

        return switch (effectiveSchema.getType()) {
            case RECORD -> jsonToGenericRecord(jsonNode, effectiveSchema);
            case ARRAY -> {
                List<Object> list = new ArrayList<>();
                Schema elementSchema = effectiveSchema.getElementType();
                for (JsonNode item : jsonNode) {
                    list.add(convertJsonToAvro(item, elementSchema));
                }
                yield list;
            }
            case MAP -> {
                Map<String, Object> map = new LinkedHashMap<>();
                Schema valueSchema = effectiveSchema.getValueType();
                jsonNode.fields().forEachRemaining(entry ->
                        map.put(entry.getKey(), convertJsonToAvro(entry.getValue(), valueSchema)));
                yield map;
            }
            case ENUM -> new GenericData.EnumSymbol(effectiveSchema, jsonNode.asText());
            case STRING -> jsonNode.asText();
            case INT -> jsonNode.asInt();
            case LONG -> jsonNode.asLong();
            case FLOAT -> (float) jsonNode.asDouble();
            case DOUBLE -> jsonNode.asDouble();
            case BOOLEAN -> jsonNode.asBoolean();
            case BYTES -> ByteBuffer.wrap(Base64.getDecoder().decode(jsonNode.asText()));
            case FIXED -> new GenericData.Fixed(effectiveSchema, Base64.getDecoder().decode(jsonNode.asText()));
            case NULL -> null;
            default -> jsonNode.asText();
        };
    }

    private KafkaProducer<String, Object> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", kafkaProperties.getSchemaRegistryUrl());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        Map<String, Object> sslProps = new HashMap<>();
        KafkaSslConfigurer.addKafkaSslProperties(sslProps, kafkaProperties);
        sslProps.forEach((k, v) -> props.put(k, v.toString()));

        Map<String, Object> srSslConfig = KafkaSslConfigurer.buildSchemaRegistrySslConfig(kafkaProperties);
        srSslConfig.forEach((k, v) -> props.put(k, v.toString()));

        log.info("Kafka producer created for {}", kafkaProperties.getBootstrapServers());
        return new KafkaProducer<>(props);
    }

    @PreDestroy
    public void close() {
        if (producer != null) {
            producer.close();
            log.info("Kafka producer closed");
        }
    }
}
```

**Step 2: Verify it compiles**

Run: `./mvnw -pl kafka-browser compile -q`
Expected: no errors

**Step 3: Commit**

```
feat(kafka-browser): add KafkaProducerService with JSON-to-Avro conversion
```

---

### Task 8: Create ProducerController

**Files:**
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/controller/ProducerController.java`
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/model/ProduceRequest.java`
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/model/ProduceResponse.java`
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/model/BulkProduceRequest.java`
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/model/BulkProduceResponse.java`

**Step 1: Create request/response DTOs**

`ProduceRequest.java`:
```java
package com.tools.kafkabrowser.model;

public record ProduceRequest(
        String topic,
        String schemaSubject,
        String jsonContent
) {}
```

`ProduceResponse.java`:
```java
package com.tools.kafkabrowser.model;

public record ProduceResponse(
        boolean success,
        String topic,
        int partition,
        long offset,
        String error
) {}
```

`BulkProduceRequest.java`:
```java
package com.tools.kafkabrowser.model;

import java.util.List;

public record BulkProduceRequest(
        String topic,
        String schemaSubject,
        List<String> messages
) {}
```

`BulkProduceResponse.java`:
```java
package com.tools.kafkabrowser.model;

import java.util.List;

public record BulkProduceResponse(
        int total,
        int succeeded,
        int failed,
        List<ProduceResponse> results
) {}
```

**Step 2: Create ProducerController**

```java
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
```

**Step 3: Verify it compiles**

Run: `./mvnw -pl kafka-browser compile -q`
Expected: no errors

**Step 4: Commit**

```
feat(kafka-browser): add ProducerController with single/bulk send and schema subject listing
```

---

## Phase 4: Frontend Navigation

### Task 9: Add navigation layout with Browser and Templates tabs

**Files:**
- Modify: `kafka-browser/frontend/src/app/layout.tsx`
- Create: `kafka-browser/frontend/src/components/nav-bar.tsx`
- Create: `kafka-browser/frontend/src/app/browser/page.tsx` (move existing page.tsx content here)
- Create: `kafka-browser/frontend/src/app/templates/page.tsx` (placeholder)
- Modify: `kafka-browser/frontend/src/app/page.tsx` (redirect to /browser)

**Step 1: Create the NavBar component**

File: `kafka-browser/frontend/src/components/nav-bar.tsx`

```tsx
"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import { Database, FileJson } from "lucide-react";

const navItems = [
  { href: "/browser", label: "Browser", icon: Database },
  { href: "/templates", label: "Templates", icon: FileJson },
];

export function NavBar() {
  const pathname = usePathname();

  return (
    <nav className="flex items-center gap-1 px-4 h-12 border-b border-white/[0.06] bg-[#0a0a0b] shrink-0">
      <span className="text-sm font-semibold text-foreground mr-4">
        Kafka Tools
      </span>
      {navItems.map(({ href, label, icon: Icon }) => {
        const isActive = pathname.startsWith(href);
        return (
          <Link
            key={href}
            href={href}
            className={cn(
              "flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm transition-colors",
              isActive
                ? "bg-white/[0.08] text-teal-400"
                : "text-muted-foreground hover:text-foreground hover:bg-white/[0.04]"
            )}
          >
            <Icon className="h-4 w-4" />
            {label}
          </Link>
        );
      })}
    </nav>
  );
}
```

**Step 2: Update layout.tsx to include NavBar**

Replace the `layout.tsx` body content to include the NavBar above the children:

```tsx
import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { JetBrains_Mono } from "next/font/google";
import { TooltipProvider } from "@/components/ui/tooltip";
import { NavBar } from "@/components/nav-bar";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

const jetbrainsMono = JetBrains_Mono({
  variable: "--font-jetbrains-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Kafka Tools",
  description: "Browse Kafka topics and produce messages with Avro serialization",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="dark">
      <body
        className={`${geistSans.variable} ${geistMono.variable} ${jetbrainsMono.variable} antialiased`}
      >
        <TooltipProvider>
          <div className="flex flex-col h-screen">
            <NavBar />
            <div className="flex-1 overflow-hidden">
              {children}
            </div>
          </div>
        </TooltipProvider>
      </body>
    </html>
  );
}
```

**Step 3: Move existing page.tsx to /browser route**

Create `kafka-browser/frontend/src/app/browser/page.tsx` with the exact content of the current `page.tsx` (the Home component with TopicSidebar + MessagePanel).

**Step 4: Update root page.tsx to redirect to /browser**

Replace `kafka-browser/frontend/src/app/page.tsx` with:

```tsx
import { redirect } from "next/navigation";

export default function Home() {
  redirect("/browser");
}
```

**Step 5: Create placeholder templates page**

Create `kafka-browser/frontend/src/app/templates/page.tsx`:

```tsx
"use client";

export default function TemplatesPage() {
  return (
    <div className="flex items-center justify-center h-full text-muted-foreground">
      Templates page — coming soon
    </div>
  );
}
```

**Step 6: Verify it builds**

Run from `kafka-browser/frontend/`: `npx next build`
Expected: builds successfully with routes /, /browser, /templates

**Step 7: Commit**

```
feat(kafka-browser): add navigation bar with Browser and Templates tabs
```

---

## Phase 5: Frontend Template Management

### Task 10: Add TypeScript types for templates and producer

**Files:**
- Modify: `kafka-browser/frontend/src/lib/types.ts`

**Step 1: Add template and producer types**

Append to the existing `types.ts`:

```typescript
export interface TemplateSummary {
  id: number;
  name: string;
  topicName: string | null;
  schemaSubject: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface TemplateDetail extends TemplateSummary {
  jsonContent: string;
}

export interface TemplateFormData {
  name: string;
  topicName: string;
  schemaSubject: string;
  jsonContent: string;
}

export interface ProduceResponse {
  success: boolean;
  topic: string;
  partition: number;
  offset: number;
  error: string | null;
}

export interface BulkProduceResponse {
  total: number;
  succeeded: number;
  failed: number;
  results: ProduceResponse[];
}
```

**Step 2: Commit**

```
feat(kafka-browser): add TypeScript types for templates and producer
```

---

### Task 11: Add API functions for templates, producer, and schemas

**Files:**
- Modify: `kafka-browser/frontend/src/lib/api.ts`

**Step 1: Add template CRUD, producer, and schema API functions**

Append to the existing `api.ts`:

```typescript
import {
  TopicInfo,
  MessagePage,
  MessageParams,
  TemplateSummary,
  TemplateDetail,
  TemplateFormData,
  ProduceResponse,
  BulkProduceResponse,
} from "./types";
```

(Update the existing import at the top to include the new types, then add:)

```typescript
// --- Templates ---

export async function fetchTemplates(): Promise<TemplateSummary[]> {
  const res = await fetch("/api/templates");
  if (!res.ok) throw new Error(`Failed to fetch templates: ${res.status}`);
  return res.json();
}

export async function fetchTemplate(id: number): Promise<TemplateDetail> {
  const res = await fetch(`/api/templates/${id}`);
  if (!res.ok) throw new Error(`Failed to fetch template: ${res.status}`);
  return res.json();
}

export async function createTemplate(data: TemplateFormData): Promise<TemplateDetail> {
  const res = await fetch("/api/templates", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error(`Failed to create template: ${res.status}`);
  return res.json();
}

export async function updateTemplate(id: number, data: TemplateFormData): Promise<TemplateDetail> {
  const res = await fetch(`/api/templates/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error(`Failed to update template: ${res.status}`);
  return res.json();
}

export async function deleteTemplate(id: number): Promise<void> {
  const res = await fetch(`/api/templates/${id}`, { method: "DELETE" });
  if (!res.ok) throw new Error(`Failed to delete template: ${res.status}`);
}

// --- Producer ---

export async function produceMessage(
  topic: string,
  jsonContent: string,
  schemaSubject?: string
): Promise<ProduceResponse> {
  const res = await fetch("/api/produce", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ topic, schemaSubject: schemaSubject || null, jsonContent }),
  });
  if (!res.ok) throw new Error(`Failed to produce message: ${res.status}`);
  return res.json();
}

export async function produceBulk(
  topic: string,
  messages: string[],
  schemaSubject?: string
): Promise<BulkProduceResponse> {
  const res = await fetch("/api/produce/bulk", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ topic, schemaSubject: schemaSubject || null, messages }),
  });
  if (!res.ok) throw new Error(`Failed to produce bulk messages: ${res.status}`);
  return res.json();
}

// --- Schemas ---

export async function fetchSchemaSubjects(): Promise<string[]> {
  const res = await fetch("/api/schemas/subjects");
  if (!res.ok) throw new Error(`Failed to fetch schema subjects: ${res.status}`);
  return res.json();
}
```

**Step 2: Commit**

```
feat(kafka-browser): add API functions for templates, producer, and schemas
```

---

### Task 12: Create custom hooks for templates, producer, and schemas

**Files:**
- Create: `kafka-browser/frontend/src/hooks/use-templates.ts`
- Create: `kafka-browser/frontend/src/hooks/use-producer.ts`
- Create: `kafka-browser/frontend/src/hooks/use-schemas.ts`

**Step 1: Create use-templates hook**

```typescript
"use client";

import { useState, useCallback, useEffect } from "react";
import { TemplateSummary, TemplateDetail, TemplateFormData } from "@/lib/types";
import {
  fetchTemplates,
  fetchTemplate,
  createTemplate,
  updateTemplate,
  deleteTemplate,
} from "@/lib/api";

export function useTemplates() {
  const [templates, setTemplates] = useState<TemplateSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchTemplates();
      setTemplates(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const get = useCallback(async (id: number): Promise<TemplateDetail | null> => {
    try {
      return await fetchTemplate(id);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      return null;
    }
  }, []);

  const create = useCallback(async (data: TemplateFormData): Promise<TemplateDetail | null> => {
    try {
      const result = await createTemplate(data);
      await refresh();
      return result;
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      return null;
    }
  }, [refresh]);

  const update = useCallback(async (id: number, data: TemplateFormData): Promise<TemplateDetail | null> => {
    try {
      const result = await updateTemplate(id, data);
      await refresh();
      return result;
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      return null;
    }
  }, [refresh]);

  const remove = useCallback(async (id: number): Promise<boolean> => {
    try {
      await deleteTemplate(id);
      await refresh();
      return true;
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      return false;
    }
  }, [refresh]);

  return { templates, loading, error, refresh, get, create, update, remove };
}
```

**Step 2: Create use-producer hook**

```typescript
"use client";

import { useState, useCallback } from "react";
import { ProduceResponse, BulkProduceResponse } from "@/lib/types";
import { produceMessage, produceBulk } from "@/lib/api";

export function useProducer() {
  const [sending, setSending] = useState(false);
  const [lastResult, setLastResult] = useState<ProduceResponse | null>(null);
  const [bulkResult, setBulkResult] = useState<BulkProduceResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const send = useCallback(async (topic: string, jsonContent: string, schemaSubject?: string) => {
    try {
      setSending(true);
      setError(null);
      const result = await produceMessage(topic, jsonContent, schemaSubject);
      setLastResult(result);
      if (!result.success) {
        setError(result.error);
      }
      return result;
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      setError(msg);
      return null;
    } finally {
      setSending(false);
    }
  }, []);

  const sendBulk = useCallback(async (topic: string, messages: string[], schemaSubject?: string) => {
    try {
      setSending(true);
      setError(null);
      setBulkResult(null);
      const result = await produceBulk(topic, messages, schemaSubject);
      setBulkResult(result);
      if (result.failed > 0) {
        setError(`${result.failed} of ${result.total} messages failed`);
      }
      return result;
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      setError(msg);
      return null;
    } finally {
      setSending(false);
    }
  }, []);

  const clearResults = useCallback(() => {
    setLastResult(null);
    setBulkResult(null);
    setError(null);
  }, []);

  return { sending, lastResult, bulkResult, error, send, sendBulk, clearResults };
}
```

**Step 3: Create use-schemas hook**

```typescript
"use client";

import { useState, useCallback, useEffect } from "react";
import { fetchSchemaSubjects } from "@/lib/api";

export function useSchemas() {
  const [subjects, setSubjects] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      setLoading(true);
      const data = await fetchSchemaSubjects();
      setSubjects(data);
    } catch {
      setSubjects([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  return { subjects, loading, refresh };
}
```

**Step 4: Verify build**

Run from `kafka-browser/frontend/`: `npx next build`
Expected: builds successfully

**Step 5: Commit**

```
feat(kafka-browser): add hooks for templates, producer, and schema subjects
```

---

## Phase 6: JSON Editor Components

### Task 13: Create editable JSON tree viewer

**Files:**
- Create: `kafka-browser/frontend/src/components/editable-json-tree.tsx`

**Step 1: Create the editable tree component**

This is based on the existing `json-tree-viewer.tsx` pattern but with editable input fields, add/delete functionality. The component works with a mutable JSON object via `onChange` callbacks.

```tsx
"use client";

import { useState, useCallback } from "react";
import { cn } from "@/lib/utils";
import { ChevronRight, X, Plus } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

interface EditableJsonTreeProps {
  data: unknown;
  onChange: (data: unknown) => void;
  defaultExpanded?: number;
}

export function EditableJsonTree({
  data,
  onChange,
  defaultExpanded = 3,
}: EditableJsonTreeProps) {
  return (
    <div className="text-sm font-mono">
      <EditableTreeNode
        value={data}
        depth={0}
        defaultExpanded={defaultExpanded}
        onChange={onChange}
        onDelete={undefined}
        isLast={true}
      />
    </div>
  );
}

interface EditableTreeNodeProps {
  label?: string;
  value: unknown;
  depth: number;
  defaultExpanded: number;
  onChange: (value: unknown) => void;
  onDelete: (() => void) | undefined;
  onRenameKey?: (oldKey: string, newKey: string) => void;
  isLast?: boolean;
}

function EditableTreeNode({
  label,
  value,
  depth,
  defaultExpanded,
  onChange,
  onDelete,
  isLast = true,
}: EditableTreeNodeProps) {
  const isObject =
    value !== null && typeof value === "object" && !Array.isArray(value);
  const isArray = Array.isArray(value);
  const isExpandable = isObject || isArray;
  const [expanded, setExpanded] = useState(depth < defaultExpanded);

  if (!isExpandable) {
    return (
      <div className="flex items-center gap-1 py-[2px] pl-4 group/leaf">
        {label !== undefined && (
          <span className="text-sky-300 shrink-0">{label}: </span>
        )}
        <LeafEditor value={value} onChange={onChange} />
        {onDelete && (
          <button
            onClick={onDelete}
            className="ml-1 opacity-0 group-hover/leaf:opacity-60 hover:!opacity-100 transition-opacity"
            title="Remove field"
          >
            <X className="h-3 w-3 text-red-400" />
          </button>
        )}
      </div>
    );
  }

  const entries = isArray
    ? (value as unknown[]).map((v, i) => [String(i), v] as const)
    : Object.entries(value as Record<string, unknown>);

  const bracketOpen = isArray ? "[" : "{";
  const bracketClose = isArray ? "]" : "}";
  const summary = isArray
    ? `${entries.length} item${entries.length !== 1 ? "s" : ""}`
    : `${entries.length} key${entries.length !== 1 ? "s" : ""}`;

  const handleChildChange = (key: string, newValue: unknown) => {
    if (isArray) {
      const arr = [...(value as unknown[])];
      arr[Number(key)] = newValue;
      onChange(arr);
    } else {
      onChange({ ...(value as Record<string, unknown>), [key]: newValue });
    }
  };

  const handleChildDelete = (key: string) => {
    if (isArray) {
      const arr = [...(value as unknown[])];
      arr.splice(Number(key), 1);
      onChange(arr);
    } else {
      const obj = { ...(value as Record<string, unknown>) };
      delete obj[key];
      onChange(obj);
    }
  };

  const handleAddField = () => {
    if (isArray) {
      onChange([...(value as unknown[]), ""]);
    } else {
      const obj = value as Record<string, unknown>;
      let newKey = "newField";
      let counter = 1;
      while (newKey in obj) {
        newKey = `newField${counter++}`;
      }
      onChange({ ...obj, [newKey]: "" });
    }
  };

  return (
    <div className="select-text">
      <div
        className={cn(
          "flex items-center gap-1 py-[2px] cursor-pointer rounded-sm hover:bg-white/[0.04] transition-colors group/node",
          depth > 0 && "pl-4"
        )}
        onClick={() => setExpanded((e) => !e)}
      >
        <ChevronRight
          className={cn(
            "h-3.5 w-3.5 shrink-0 text-muted-foreground transition-transform duration-150",
            expanded && "rotate-90"
          )}
        />
        {label !== undefined && (
          <span className="text-sky-300">{label}: </span>
        )}
        <span className="text-muted-foreground">
          {bracketOpen}
          {!expanded && (
            <span className="text-muted-foreground/60 text-xs ml-1">
              {summary}
            </span>
          )}
          {!expanded && bracketClose}
        </span>
        {onDelete && (
          <button
            onClick={(e) => {
              e.stopPropagation();
              onDelete();
            }}
            className="ml-1 opacity-0 group-hover/node:opacity-60 hover:!opacity-100 transition-opacity"
            title="Remove field"
          >
            <X className="h-3 w-3 text-red-400" />
          </button>
        )}
      </div>

      {expanded && (
        <>
          <div className="border-l border-white/[0.06] ml-[7px]">
            {entries.map(([key, val], i) => (
              <EditableTreeNode
                key={`${key}-${i}`}
                label={isArray ? undefined : key}
                value={val}
                depth={depth + 1}
                defaultExpanded={defaultExpanded}
                onChange={(newVal) => handleChildChange(key, newVal)}
                onDelete={() => handleChildDelete(key)}
                isLast={i === entries.length - 1}
              />
            ))}
            <div className="pl-4 py-1">
              <Button
                variant="ghost"
                size="xs"
                onClick={handleAddField}
                className="h-5 text-xs text-muted-foreground hover:text-teal-400"
              >
                <Plus className="h-3 w-3 mr-1" />
                {isArray ? "Add item" : "Add field"}
              </Button>
            </div>
          </div>
          <div className={cn("py-[1px]", depth > 0 && "pl-4")}>
            <span className="text-muted-foreground">{bracketClose}</span>
          </div>
        </>
      )}
    </div>
  );
}

function LeafEditor({
  value,
  onChange,
}: {
  value: unknown;
  onChange: (value: unknown) => void;
}) {
  const stringValue = value === null ? "null" : String(value);
  const [editValue, setEditValue] = useState(stringValue);

  const handleBlur = () => {
    if (editValue === "null") {
      onChange(null);
    } else if (editValue === "true") {
      onChange(true);
    } else if (editValue === "false") {
      onChange(false);
    } else if (editValue !== "" && !isNaN(Number(editValue))) {
      onChange(Number(editValue));
    } else {
      onChange(editValue);
    }
  };

  const colorClass =
    value === null
      ? "text-zinc-500"
      : typeof value === "boolean"
        ? "text-violet-400"
        : typeof value === "number"
          ? "text-amber-400"
          : "text-emerald-400";

  return (
    <Input
      value={editValue}
      onChange={(e) => setEditValue(e.target.value)}
      onBlur={handleBlur}
      className={cn(
        "h-5 px-1 py-0 text-xs border-none bg-transparent focus-visible:bg-white/[0.06] focus-visible:ring-1 focus-visible:ring-teal-400/50 rounded-sm",
        colorClass
      )}
      style={{ width: `${Math.max(editValue.length + 2, 6)}ch` }}
    />
  );
}
```

**Step 2: Verify build**

Run from `kafka-browser/frontend/`: `npx next build`
Expected: builds successfully

**Step 3: Commit**

```
feat(kafka-browser): add editable JSON tree component for template editing
```

---

### Task 14: Create template list sidebar component

**Files:**
- Create: `kafka-browser/frontend/src/components/template-list.tsx`

**Step 1: Create the component**

```tsx
"use client";

import { TemplateSummary } from "@/lib/types";
import { cn, formatTimestamp } from "@/lib/utils";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Upload, FileJson, Trash2, FolderOpen } from "lucide-react";
import { useCallback, useRef } from "react";

interface TemplateListProps {
  templates: TemplateSummary[];
  loading: boolean;
  selectedId: number | null;
  onSelect: (id: number) => void;
  onUpload: (name: string, content: string) => void;
  onBulkUpload: (files: { name: string; content: string }[]) => void;
  onDelete: (id: number) => void;
}

export function TemplateList({
  templates,
  loading,
  selectedId,
  onSelect,
  onUpload,
  onBulkUpload,
  onDelete,
}: TemplateListProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const bulkInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>) => {
      const files = e.target.files;
      if (!files?.length) return;
      const file = files[0];
      const content = await file.text();
      const name = file.name.replace(/\.json$/i, "");
      onUpload(name, content);
      e.target.value = "";
    },
    [onUpload]
  );

  const handleBulkChange = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>) => {
      const files = e.target.files;
      if (!files?.length) return;
      const results: { name: string; content: string }[] = [];
      for (const file of Array.from(files)) {
        if (!file.name.endsWith(".json")) continue;
        const content = await file.text();
        results.push({ name: file.name.replace(/\.json$/i, ""), content });
      }
      if (results.length > 0) onBulkUpload(results);
      e.target.value = "";
    },
    [onBulkUpload]
  );

  const handleDrop = useCallback(
    async (e: React.DragEvent) => {
      e.preventDefault();
      const files = Array.from(e.dataTransfer.files).filter((f) =>
        f.name.endsWith(".json")
      );
      if (files.length === 1) {
        const content = await files[0].text();
        const name = files[0].name.replace(/\.json$/i, "");
        onUpload(name, content);
      } else if (files.length > 1) {
        const results: { name: string; content: string }[] = [];
        for (const file of files) {
          const content = await file.text();
          results.push({ name: file.name.replace(/\.json$/i, ""), content });
        }
        onBulkUpload(results);
      }
    },
    [onUpload, onBulkUpload]
  );

  return (
    <div
      className="w-[260px] shrink-0 border-r border-white/[0.06] flex flex-col h-full"
      onDragOver={(e) => e.preventDefault()}
      onDrop={handleDrop}
    >
      <div className="p-3 border-b border-white/[0.06]">
        <h2 className="text-sm font-semibold text-foreground mb-2">Templates</h2>
        <div className="flex gap-1">
          <Button
            variant="outline"
            size="xs"
            onClick={() => fileInputRef.current?.click()}
            className="flex-1 text-xs"
          >
            <Upload className="h-3 w-3 mr-1" />
            Upload
          </Button>
          <Button
            variant="outline"
            size="xs"
            onClick={() => bulkInputRef.current?.click()}
            className="flex-1 text-xs"
          >
            <FolderOpen className="h-3 w-3 mr-1" />
            Bulk
          </Button>
        </div>
        <input
          ref={fileInputRef}
          type="file"
          accept=".json"
          onChange={handleFileChange}
          className="hidden"
        />
        <input
          ref={bulkInputRef}
          type="file"
          accept=".json"
          multiple
          onChange={handleBulkChange}
          className="hidden"
        />
      </div>

      <ScrollArea className="flex-1">
        <div className="p-2">
          {loading ? (
            Array.from({ length: 5 }).map((_, i) => (
              <Skeleton key={i} className="h-12 mb-1 rounded-md" />
            ))
          ) : templates.length === 0 ? (
            <div className="text-center text-muted-foreground text-xs py-8">
              <FileJson className="h-8 w-8 mx-auto mb-2 opacity-40" />
              <p>No templates yet</p>
              <p className="mt-1">Upload a JSON file or drag & drop</p>
            </div>
          ) : (
            templates.map((t) => (
              <div
                key={t.id}
                onClick={() => onSelect(t.id)}
                className={cn(
                  "flex items-center justify-between px-2 py-2 rounded-md cursor-pointer transition-colors group/item mb-0.5",
                  selectedId === t.id
                    ? "bg-teal-400/10 border-l-2 border-teal-400"
                    : "hover:bg-white/[0.04]"
                )}
              >
                <div className="min-w-0 flex-1">
                  <p className="text-sm text-foreground truncate">{t.name}</p>
                  {t.topicName && (
                    <p className="text-[10px] text-muted-foreground font-mono truncate">
                      {t.topicName}
                    </p>
                  )}
                </div>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onDelete(t.id);
                  }}
                  className="opacity-0 group-hover/item:opacity-60 hover:!opacity-100 transition-opacity ml-2"
                  title="Delete template"
                >
                  <Trash2 className="h-3.5 w-3.5 text-red-400" />
                </button>
              </div>
            ))
          )}
        </div>
      </ScrollArea>

      <div className="p-2 border-t border-white/[0.06] text-[10px] text-muted-foreground text-center">
        {templates.length} template{templates.length !== 1 ? "s" : ""} · Drop JSON files here
      </div>
    </div>
  );
}
```

**Step 2: Commit**

```
feat(kafka-browser): add template list sidebar with upload, bulk upload, and drag-and-drop
```

---

### Task 15: Create template editor component with tree/raw toggle

**Files:**
- Create: `kafka-browser/frontend/src/components/template-editor.tsx`

**Step 1: Create the editor component**

This wraps the editable JSON tree and a raw textarea with a toggle between them. It also includes the name, topic, schema subject fields and Save/Send buttons.

```tsx
"use client";

import { useState, useEffect, useCallback } from "react";
import { EditableJsonTree } from "@/components/editable-json-tree";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Save, Send, TreePine, Code, AlertCircle } from "lucide-react";

interface TemplateEditorProps {
  name: string;
  topicName: string;
  schemaSubject: string;
  jsonContent: string;
  subjects: string[];
  onNameChange: (name: string) => void;
  onTopicChange: (topic: string) => void;
  onSchemaSubjectChange: (subject: string) => void;
  onJsonChange: (json: string) => void;
  onSave: () => void;
  onSend: () => void;
  saving: boolean;
  sending: boolean;
  sendResult?: { success: boolean; message: string } | null;
}

export function TemplateEditor({
  name,
  topicName,
  schemaSubject,
  jsonContent,
  subjects,
  onNameChange,
  onTopicChange,
  onSchemaSubjectChange,
  onJsonChange,
  onSave,
  onSend,
  saving,
  sending,
  sendResult,
}: TemplateEditorProps) {
  const [viewMode, setViewMode] = useState<"tree" | "raw">("tree");
  const [parsedJson, setParsedJson] = useState<unknown>(null);
  const [parseError, setParseError] = useState<string | null>(null);
  const [rawContent, setRawContent] = useState(jsonContent);

  useEffect(() => {
    setRawContent(jsonContent);
    try {
      setParsedJson(JSON.parse(jsonContent));
      setParseError(null);
    } catch (e) {
      setParsedJson(null);
      setParseError(e instanceof Error ? e.message : "Invalid JSON");
    }
  }, [jsonContent]);

  const handleTreeChange = useCallback(
    (newData: unknown) => {
      setParsedJson(newData);
      const newJson = JSON.stringify(newData, null, 2);
      setRawContent(newJson);
      onJsonChange(newJson);
    },
    [onJsonChange]
  );

  const handleRawChange = useCallback(
    (newRaw: string) => {
      setRawContent(newRaw);
      try {
        const parsed = JSON.parse(newRaw);
        setParsedJson(parsed);
        setParseError(null);
        onJsonChange(newRaw);
      } catch (e) {
        setParseError(e instanceof Error ? e.message : "Invalid JSON");
      }
    },
    [onJsonChange]
  );

  return (
    <div className="flex flex-col h-full">
      {/* Top controls */}
      <div className="p-3 border-b border-white/[0.06] space-y-2">
        <div className="flex gap-2">
          <Input
            value={name}
            onChange={(e) => onNameChange(e.target.value)}
            placeholder="Template name"
            className="h-8 text-sm"
          />
        </div>
        <div className="flex gap-2">
          <Input
            value={topicName}
            onChange={(e) => onTopicChange(e.target.value)}
            placeholder="Target topic"
            className="h-8 text-sm flex-1 font-mono"
          />
          <Input
            value={schemaSubject}
            onChange={(e) => onSchemaSubjectChange(e.target.value)}
            placeholder="Schema subject (optional)"
            className="h-8 text-sm flex-1 font-mono"
            list="schema-subjects"
          />
          <datalist id="schema-subjects">
            {subjects.map((s) => (
              <option key={s} value={s} />
            ))}
          </datalist>
        </div>
      </div>

      {/* View mode toggle */}
      <div className="flex items-center gap-1 px-3 py-2 border-b border-white/[0.06]">
        <Button
          variant={viewMode === "tree" ? "default" : "ghost"}
          size="xs"
          onClick={() => setViewMode("tree")}
          className="h-6 text-xs"
        >
          <TreePine className="h-3 w-3 mr-1" />
          Tree
        </Button>
        <Button
          variant={viewMode === "raw" ? "default" : "ghost"}
          size="xs"
          onClick={() => setViewMode("raw")}
          className="h-6 text-xs"
        >
          <Code className="h-3 w-3 mr-1" />
          Raw JSON
        </Button>
        {parseError && viewMode === "raw" && (
          <span className="ml-2 text-xs text-red-400 flex items-center gap-1">
            <AlertCircle className="h-3 w-3" />
            {parseError}
          </span>
        )}
      </div>

      {/* Editor area */}
      <ScrollArea className="flex-1">
        <div className="p-3">
          {viewMode === "tree" ? (
            parsedJson !== null ? (
              <EditableJsonTree
                data={parsedJson}
                onChange={handleTreeChange}
                defaultExpanded={3}
              />
            ) : (
              <div className="text-red-400 text-sm">
                <AlertCircle className="h-4 w-4 inline mr-1" />
                Cannot render tree: {parseError}
              </div>
            )
          ) : (
            <textarea
              value={rawContent}
              onChange={(e) => handleRawChange(e.target.value)}
              className="w-full min-h-[400px] bg-transparent text-sm font-mono text-foreground resize-none focus:outline-none focus:ring-1 focus:ring-teal-400/50 rounded-md p-2 border border-white/[0.06]"
              spellCheck={false}
            />
          )}
        </div>
      </ScrollArea>

      {/* Bottom actions */}
      <div className="p-3 border-t border-white/[0.06] flex items-center gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={onSave}
          disabled={saving || !name.trim()}
        >
          <Save className="h-4 w-4 mr-1" />
          {saving ? "Saving..." : "Save"}
        </Button>
        <Button
          size="sm"
          onClick={onSend}
          disabled={sending || !topicName.trim() || parseError !== null}
          className="bg-teal-600 hover:bg-teal-700 text-white"
        >
          <Send className="h-4 w-4 mr-1" />
          {sending ? "Sending..." : "Send to Kafka"}
        </Button>
        {sendResult && (
          <span
            className={`text-xs ${sendResult.success ? "text-emerald-400" : "text-red-400"}`}
          >
            {sendResult.message}
          </span>
        )}
      </div>
    </div>
  );
}
```

**Step 2: Commit**

```
feat(kafka-browser): add template editor with tree/raw JSON toggle and send controls
```

---

## Phase 7: Bulk Send

### Task 16: Create bulk send dialog component

**Files:**
- Create: `kafka-browser/frontend/src/components/bulk-send-dialog.tsx`

**Step 1: Create the component**

```tsx
"use client";

import { useState, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import {
  Send,
  X,
  FileJson,
  CheckCircle,
  XCircle,
  Loader2,
} from "lucide-react";
import { BulkProduceResponse } from "@/lib/types";

interface BulkSendFile {
  name: string;
  content: string;
}

interface BulkSendDialogProps {
  files: BulkSendFile[];
  subjects: string[];
  onSend: (
    topic: string,
    messages: string[],
    schemaSubject?: string
  ) => Promise<BulkProduceResponse | null>;
  onClose: () => void;
}

export function BulkSendDialog({
  files,
  subjects,
  onSend,
  onClose,
}: BulkSendDialogProps) {
  const [topic, setTopic] = useState("");
  const [schemaSubject, setSchemaSubject] = useState("");
  const [sending, setSending] = useState(false);
  const [result, setResult] = useState<BulkProduceResponse | null>(null);

  const handleSend = useCallback(async () => {
    if (!topic.trim() || files.length === 0) return;
    setSending(true);
    const messages = files.map((f) => f.content);
    const res = await onSend(
      topic,
      messages,
      schemaSubject || undefined
    );
    setResult(res);
    setSending(false);
  }, [topic, schemaSubject, files, onSend]);

  return (
    <div className="flex flex-col h-full">
      <div className="p-3 border-b border-white/[0.06] flex items-center justify-between">
        <h3 className="text-sm font-semibold text-foreground">
          Bulk Send — {files.length} file{files.length !== 1 ? "s" : ""}
        </h3>
        <button onClick={onClose} className="text-muted-foreground hover:text-foreground">
          <X className="h-4 w-4" />
        </button>
      </div>

      <div className="p-3 border-b border-white/[0.06] space-y-2">
        <Input
          value={topic}
          onChange={(e) => setTopic(e.target.value)}
          placeholder="Target topic"
          className="h-8 text-sm font-mono"
        />
        <Input
          value={schemaSubject}
          onChange={(e) => setSchemaSubject(e.target.value)}
          placeholder="Schema subject (optional)"
          className="h-8 text-sm font-mono"
          list="bulk-schema-subjects"
        />
        <datalist id="bulk-schema-subjects">
          {subjects.map((s) => (
            <option key={s} value={s} />
          ))}
        </datalist>
      </div>

      <ScrollArea className="flex-1">
        <div className="p-3 space-y-1">
          {files.map((file, i) => (
            <div
              key={i}
              className="flex items-center gap-2 px-2 py-1.5 rounded-md bg-white/[0.02] text-sm"
            >
              <FileJson className="h-4 w-4 text-muted-foreground shrink-0" />
              <span className="flex-1 truncate font-mono text-xs">
                {file.name}.json
              </span>
              <Badge variant="outline" className="text-[10px]">
                {(file.content.length / 1024).toFixed(1)}KB
              </Badge>
              {result && result.results[i] && (
                result.results[i].success ? (
                  <CheckCircle className="h-4 w-4 text-emerald-400 shrink-0" />
                ) : (
                  <XCircle className="h-4 w-4 text-red-400 shrink-0" title={result.results[i].error || undefined} />
                )
              )}
            </div>
          ))}
        </div>
      </ScrollArea>

      <div className="p-3 border-t border-white/[0.06] flex items-center gap-2">
        {result ? (
          <>
            <span className="text-sm">
              <span className="text-emerald-400">{result.succeeded} sent</span>
              {result.failed > 0 && (
                <span className="text-red-400 ml-2">{result.failed} failed</span>
              )}
            </span>
            <Button variant="outline" size="sm" onClick={onClose} className="ml-auto">
              Close
            </Button>
          </>
        ) : (
          <Button
            size="sm"
            onClick={handleSend}
            disabled={sending || !topic.trim()}
            className="bg-teal-600 hover:bg-teal-700 text-white ml-auto"
          >
            {sending ? (
              <Loader2 className="h-4 w-4 mr-1 animate-spin" />
            ) : (
              <Send className="h-4 w-4 mr-1" />
            )}
            {sending ? "Sending..." : `Send ${files.length} messages`}
          </Button>
        )}
      </div>
    </div>
  );
}
```

**Step 2: Commit**

```
feat(kafka-browser): add bulk send dialog for multi-file Kafka producing
```

---

## Phase 8: Wire Up Templates Page

### Task 17: Build the full Templates page

**Files:**
- Modify: `kafka-browser/frontend/src/app/templates/page.tsx`

**Step 1: Replace the placeholder with the full page**

This page orchestrates all the components: template list, template editor, and bulk send dialog.

```tsx
"use client";

import { useState, useCallback } from "react";
import { TemplateList } from "@/components/template-list";
import { TemplateEditor } from "@/components/template-editor";
import { BulkSendDialog } from "@/components/bulk-send-dialog";
import { useTemplates } from "@/hooks/use-templates";
import { useProducer } from "@/hooks/use-producer";
import { useSchemas } from "@/hooks/use-schemas";
import { FileJson } from "lucide-react";

interface BulkFile {
  name: string;
  content: string;
}

export default function TemplatesPage() {
  const { templates, loading, get, create, update, remove, refresh } = useTemplates();
  const { send, sendBulk } = useProducer();
  const { subjects } = useSchemas();

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [name, setName] = useState("");
  const [topicName, setTopicName] = useState("");
  const [schemaSubject, setSchemaSubject] = useState("");
  const [jsonContent, setJsonContent] = useState("");
  const [saving, setSaving] = useState(false);
  const [sending, setSending] = useState(false);
  const [sendResult, setSendResult] = useState<{
    success: boolean;
    message: string;
  } | null>(null);
  const [bulkFiles, setBulkFiles] = useState<BulkFile[] | null>(null);

  const loadTemplate = useCallback(
    async (id: number) => {
      const template = await get(id);
      if (template) {
        setSelectedId(template.id);
        setName(template.name);
        setTopicName(template.topicName || "");
        setSchemaSubject(template.schemaSubject || "");
        setJsonContent(template.jsonContent || "{}");
        setSendResult(null);
      }
    },
    [get]
  );

  const handleUpload = useCallback(
    async (fileName: string, content: string) => {
      // Validate JSON
      try {
        JSON.parse(content);
      } catch {
        return;
      }

      const result = await create({
        name: fileName,
        topicName: "",
        schemaSubject: "",
        jsonContent: content,
      });

      if (result) {
        setSelectedId(result.id);
        setName(result.name);
        setTopicName(result.topicName || "");
        setSchemaSubject(result.schemaSubject || "");
        setJsonContent(result.jsonContent || "{}");
        setSendResult(null);
      }
    },
    [create]
  );

  const handleBulkUpload = useCallback((files: BulkFile[]) => {
    setBulkFiles(files);
  }, []);

  const handleDelete = useCallback(
    async (id: number) => {
      await remove(id);
      if (selectedId === id) {
        setSelectedId(null);
        setName("");
        setTopicName("");
        setSchemaSubject("");
        setJsonContent("");
        setSendResult(null);
      }
    },
    [remove, selectedId]
  );

  const handleSave = useCallback(async () => {
    setSaving(true);
    const data = { name, topicName, schemaSubject, jsonContent };
    if (selectedId) {
      await update(selectedId, data);
    } else {
      const result = await create(data);
      if (result) setSelectedId(result.id);
    }
    setSaving(false);
  }, [selectedId, name, topicName, schemaSubject, jsonContent, create, update]);

  const handleSend = useCallback(async () => {
    if (!topicName.trim()) return;
    setSending(true);
    setSendResult(null);
    const result = await send(
      topicName,
      jsonContent,
      schemaSubject || undefined
    );
    if (result) {
      setSendResult(
        result.success
          ? { success: true, message: `Sent to partition ${result.partition}, offset ${result.offset}` }
          : { success: false, message: result.error || "Failed to send" }
      );
    }
    setSending(false);
  }, [topicName, jsonContent, schemaSubject, send]);

  const hasContent = jsonContent && jsonContent !== "{}";

  return (
    <div className="flex h-full overflow-hidden">
      <TemplateList
        templates={templates}
        loading={loading}
        selectedId={selectedId}
        onSelect={loadTemplate}
        onUpload={handleUpload}
        onBulkUpload={handleBulkUpload}
        onDelete={handleDelete}
      />
      <div className="flex-1 overflow-hidden">
        {bulkFiles ? (
          <BulkSendDialog
            files={bulkFiles}
            subjects={subjects}
            onSend={sendBulk}
            onClose={() => setBulkFiles(null)}
          />
        ) : hasContent ? (
          <TemplateEditor
            name={name}
            topicName={topicName}
            schemaSubject={schemaSubject}
            jsonContent={jsonContent}
            subjects={subjects}
            onNameChange={setName}
            onTopicChange={setTopicName}
            onSchemaSubjectChange={setSchemaSubject}
            onJsonChange={setJsonContent}
            onSave={handleSave}
            onSend={handleSend}
            saving={saving}
            sending={sending}
            sendResult={sendResult}
          />
        ) : (
          <div className="flex flex-col items-center justify-center h-full text-muted-foreground">
            <FileJson className="h-16 w-16 mb-4 opacity-30" />
            <p className="text-lg">No template selected</p>
            <p className="text-sm mt-1">
              Select a template from the list, or upload a JSON file
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
```

**Step 2: Verify frontend build**

Run from `kafka-browser/frontend/`: `npx next build`
Expected: builds successfully with all routes

**Step 3: Verify backend compiles**

Run from project root: `./mvnw -pl kafka-browser compile -q`
Expected: no errors

**Step 4: Commit**

```
feat(kafka-browser): wire up full templates page with editor, list, and bulk send
```

---

## Phase 9: Final Verification

### Task 18: End-to-end verification

**Step 1: Start Docker infrastructure**

```bash
docker-compose up -d
```

Wait for Kafka, Zookeeper, and Schema Registry to be ready.

**Step 2: Start the backend**

```bash
cd kafka-browser
../mvnw spring-boot:run
```

Verify: `curl http://localhost:8080/api/health` returns `{"status":"UP"}`

**Step 3: Verify template CRUD**

```bash
# Create a template
curl -X POST http://localhost:8080/api/templates \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Template","topicName":"billing-engine-inbound","jsonContent":"{\"amount\":100}"}'

# List templates
curl http://localhost:8080/api/templates

# Get by ID
curl http://localhost:8080/api/templates/1

# Delete
curl -X DELETE http://localhost:8080/api/templates/1
```

**Step 4: Verify schema subjects**

```bash
curl http://localhost:8080/api/schemas/subjects
```

**Step 5: Start the frontend**

```bash
cd kafka-browser/frontend
npm run dev
```

Open `http://localhost:3000` — verify navigation between Browser and Templates tabs.

**Step 6: Manual UI verification**

1. Upload a JSON file via the Templates page
2. Edit a field value in the tree view
3. Switch to Raw JSON — confirm the edit is reflected
4. Edit in Raw JSON — switch back to tree, confirm sync
5. Save the template
6. Reload the page — verify template persists
7. Send a message (if Kafka is running)

**Step 7: Final commit**

```
chore(kafka-browser): verify end-to-end producer and templates functionality
```

---

## Summary

| Phase | Tasks | Description |
|-------|-------|-------------|
| 1 | 1-3 | Backend infra: JPA + H2, shared SchemaRegistryClient |
| 2 | 4-6 | Template CRUD: entity, repo, controller |
| 3 | 7-8 | Kafka producer: service + controller |
| 4 | 9 | Frontend navigation restructure |
| 5 | 10-12 | Frontend hooks, types, API functions |
| 6 | 13-14 | JSON editor components (tree + list sidebar) |
| 7 | 15-16 | Template editor + bulk send dialog |
| 8 | 17 | Wire up Templates page |
| 9 | 18 | End-to-end verification |
