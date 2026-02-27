# Kafka Producer & Templates Feature Design

**Date:** 2026-02-27
**Status:** Approved
**Module:** kafka-browser (backend + frontend)

## Purpose

Extend the kafka-browser tool to support **producing messages to Kafka** via a web UI. Users upload JSON templates, edit them visually, save them to a local database, and send them to Kafka topics as Avro-serialized messages. This replicates the test framework's producer functionality in a UI-driven workflow for manual testing.

## Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Architecture | Extend existing kafka-browser module | Single deployable, shared Kafka config |
| Database | H2 file-based (JPA) | Zero setup, survives restarts, Spring Boot native |
| Serialization | Avro via Schema Registry | Matches real billing engine message format |
| Dynamic values | Not included (v1) | YAGNI — static values only for now |
| Topic selection | User-input field, saved with template | Flexible, no hardcoded topics |
| JSON editor | Tree view + Raw JSON toggle | Visual editing for most users, raw for power users |
| Bulk send | Sequential per-file upload | User uploads folder of JSONs, sent one by one |

## Backend Design

### Dependencies (additions to pom.xml)

- `spring-boot-starter-data-jpa`
- `com.h2database:h2`

### H2 Configuration (application.yml)

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

### Template Entity

```java
@Entity
@Table(name = "templates")
public class Template {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;           // User-given name
    private String topicName;      // Target Kafka topic
    private String schemaSubject;  // Avro schema subject (optional)

    @Column(columnDefinition = "CLOB")
    private String jsonContent;    // Full JSON template

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### REST API — TemplateController

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/templates` | List all templates (id, name, topic, timestamps) |
| `GET` | `/api/templates/{id}` | Get full template with JSON content |
| `POST` | `/api/templates` | Create new template |
| `PUT` | `/api/templates/{id}` | Update template |
| `DELETE` | `/api/templates/{id}` | Delete template |

### REST API — ProducerController

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/produce` | Send single message: `{ jsonContent, topic, schemaSubject? }` |
| `POST` | `/api/produce/bulk` | Send multiple messages sequentially: `{ messages: [...], topic, schemaSubject? }` |
| `GET` | `/api/schemas/subjects` | List available schema subjects from Schema Registry |

### KafkaProducerService

Flow for producing a message:
1. Receive JSON content + topic + schema subject
2. If schema subject provided: fetch latest Avro schema from Schema Registry
3. Convert JSON string → Avro `GenericRecord` using the schema (reverse of existing `avroToJson`)
4. Serialize with Confluent `KafkaAvroSerializer` (magic byte + schema ID + binary)
5. Produce to target topic via `KafkaProducer`
6. If no schema subject: send as raw JSON string

### Shared SchemaRegistryClient

Extract `SchemaRegistryClient` creation from `KafkaReaderService` constructor into a `@Bean` in a new `SchemaRegistryConfig` class. Both `KafkaReaderService` and `KafkaProducerService` inject this shared bean.

## Frontend Design

### Navigation

Add horizontal nav bar to `layout.tsx`:
- **Browser** tab → existing topic browser (`/browser` or `/`)
- **Templates** tab → new templates page (`/templates`)

### Templates Page Layout

```
┌─────────────────────────────────────────────────┐
│  [Browser]  [Templates]                    nav  │
├──────────────┬──────────────────────────────────┤
│              │                                  │
│  Template    │   Template Editor               │
│  List        │                                  │
│              │   [Tree View] [Raw JSON]  toggle │
│  - SEPA RT   │                                  │
│  - SEPA STD  │   ┌──────────────────────────┐  │
│  - Custom 1  │   │ body                     │  │
│              │   │  ├─ amount: [69999.99]   │  │
│  [+ Upload]  │   │  ├─ accountNr: [99991..] │  │
│  [Bulk Send] │   │  └─ txCode: [DM]         │  │
│              │   └──────────────────────────┘  │
│              │                                  │
│              │   Name: [SEPA RT Standard]       │
│              │   Topic: [billing-engine-inbound] │
│              │   Schema: [billing-engine-in..▼]  │
│              │                                  │
│              │   [Save]  [Send to Kafka]        │
└──────────────┴──────────────────────────────────┘
```

### Components

1. **TemplateList** — Left sidebar with saved templates. Click to load. Upload button triggers drag & drop zone + file picker.

2. **TemplateEditor** — Main editor area with tab toggle:
   - **Tree View**: Recursive editable tree (based on existing `json-tree-viewer.tsx` pattern). Leaf nodes have inline input fields. Delete (X) button per node. "Add field" button on objects/arrays.
   - **Raw JSON View**: Textarea/code editor showing raw JSON. Bidirectional sync with tree view.

3. **SendControls** — Bottom panel with:
   - Name input (for saving)
   - Topic input field (saved with template)
   - Schema subject dropdown (fetched from `/api/schemas/subjects`)
   - Save button, Send to Kafka button

4. **BulkSendDialog** — Modal/panel for bulk operations:
   - Drag & drop zone for multiple files or folder
   - File list with names and sizes
   - Shared topic + schema subject fields
   - "Send All" button with sequential progress indicator
   - Per-file success/fail status display

### Hooks

- `use-templates.ts` — CRUD operations for templates (fetch list, get by id, create, update, delete)
- `use-producer.ts` — Send single/bulk messages, track send status
- `use-schemas.ts` — Fetch available schema subjects

## Data Flow

```
Upload JSON ──→ Parse & Display ──→ Edit (Tree/Raw) ──→ Save to H2
                                                          │
                                    ┌─────────────────────┘
                                    ▼
                              [Send to Kafka]
                                    │
                              ┌─────▼─────┐
                              │ Backend    │
                              │            │
                              │ 1. Fetch schema from SR
                              │ 2. JSON → GenericRecord
                              │ 3. Avro serialize
                              │ 4. KafkaProducer.send()
                              └────────────┘

Bulk Upload ──→ Display file list ──→ Send All (sequential)
```

## Error Handling

- **Schema mismatch**: Backend returns 400 with field-level error details
- **Send failure**: Frontend shows per-message success/fail in bulk mode
- **Invalid JSON**: Frontend validates on parse/edit, disables Send for invalid JSON
- **Schema Registry unreachable**: Graceful degradation — show error, allow raw JSON send
