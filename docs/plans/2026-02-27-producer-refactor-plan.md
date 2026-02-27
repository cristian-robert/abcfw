# Producer Page Refactor — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Refactor the Templates page into a Producer page with database-backed collections, auto-generated form fields, .avsc schema support, collection-level Kafka sending, confirmation dialogs, and a three-panel Postman-style layout.

**Architecture:** Backend adds a `Collection` JPA entity with one-to-many relationship to `Template`. Template loses `topicName`/`schemaSubject` (moved to Collection). Frontend is rewritten as three-panel layout: CollectionSidebar | TemplateListPanel | FormEditor. Old components (`editable-json-tree.tsx`, `template-list.tsx`, `template-editor.tsx`, `bulk-send-dialog.tsx`) are removed and replaced.

**Tech Stack:** Java 17 + Spring Boot 3.5.6 + JPA/H2, Next.js 16 + React 19 + Tailwind 4 + shadcn/ui, Confluent Kafka + Avro

**Design Doc:** `docs/plans/2026-02-27-producer-refactor-design.md`

---

## Task 1: Collection Entity & Repository (Backend)

**Files:**
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/model/Collection.java`
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/repository/CollectionRepository.java`

**Step 1: Create Collection entity**

```java
// src/main/java/com/tools/kafkabrowser/model/Collection.java
package com.tools.kafkabrowser.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "collections")
@Getter
@Setter
@NoArgsConstructor
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "CLOB")
    private String avscContent;

    private String schemaSubject;

    private String topicName;

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

**Step 2: Create CollectionRepository**

```java
// src/main/java/com/tools/kafkabrowser/repository/CollectionRepository.java
package com.tools.kafkabrowser.repository;

import com.tools.kafkabrowser.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findAllByOrderByUpdatedAtDesc();
}
```

**Step 3: Verify compilation**

Run: `cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser && ./mvnw compile -q`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add src/main/java/com/tools/kafkabrowser/model/Collection.java src/main/java/com/tools/kafkabrowser/repository/CollectionRepository.java
git commit -m "feat: add Collection entity and repository"
```

---

## Task 2: Modify Template Entity (Backend)

**Files:**
- Modify: `kafka-browser/src/main/java/com/tools/kafkabrowser/model/Template.java`

**Step 1: Add collectionId FK, remove topicName and schemaSubject**

Replace the entire `Template.java` with:

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

    @Column(columnDefinition = "CLOB")
    private String jsonContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

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

**Step 2: Verify compilation**

Run: `cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser && ./mvnw compile -q`
Expected: FAIL — TemplateDto, TemplateSummaryDto, and TemplateController reference removed fields. Fix in next tasks.

**Step 3: Do NOT commit yet** — wait until DTOs and controller are updated.

---

## Task 3: Update DTOs (Backend)

**Files:**
- Modify: `kafka-browser/src/main/java/com/tools/kafkabrowser/model/TemplateDto.java`
- Modify: `kafka-browser/src/main/java/com/tools/kafkabrowser/model/TemplateSummaryDto.java`
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/model/CollectionDto.java`
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/model/CollectionSummaryDto.java`

**Step 1: Update TemplateDto — remove topicName/schemaSubject, add collectionId**

```java
// src/main/java/com/tools/kafkabrowser/model/TemplateDto.java
package com.tools.kafkabrowser.model;

public record TemplateDto(
        String name,
        String jsonContent,
        Long collectionId
) {}
```

**Step 2: Update TemplateSummaryDto — remove topicName/schemaSubject, add collectionId**

```java
// src/main/java/com/tools/kafkabrowser/model/TemplateSummaryDto.java
package com.tools.kafkabrowser.model;

import java.time.LocalDateTime;

public record TemplateSummaryDto(
        Long id,
        String name,
        Long collectionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TemplateSummaryDto from(Template template) {
        return new TemplateSummaryDto(
                template.getId(),
                template.getName(),
                template.getCollection() != null ? template.getCollection().getId() : null,
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
```

**Step 3: Create CollectionDto**

```java
// src/main/java/com/tools/kafkabrowser/model/CollectionDto.java
package com.tools.kafkabrowser.model;

public record CollectionDto(
        String name,
        String topicName,
        String schemaSubject,
        String avscContent
) {}
```

**Step 4: Create CollectionSummaryDto**

```java
// src/main/java/com/tools/kafkabrowser/model/CollectionSummaryDto.java
package com.tools.kafkabrowser.model;

import java.time.LocalDateTime;

public record CollectionSummaryDto(
        Long id,
        String name,
        String topicName,
        String schemaSubject,
        boolean hasAvsc,
        int templateCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CollectionSummaryDto from(Collection collection, int templateCount) {
        return new CollectionSummaryDto(
                collection.getId(),
                collection.getName(),
                collection.getTopicName(),
                collection.getSchemaSubject(),
                collection.getAvscContent() != null && !collection.getAvscContent().isBlank(),
                templateCount,
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }
}
```

**Step 5: Verify compilation** (will still fail until controller is updated)

---

## Task 4: Update TemplateController & Create CollectionController (Backend)

**Files:**
- Modify: `kafka-browser/src/main/java/com/tools/kafkabrowser/controller/TemplateController.java`
- Create: `kafka-browser/src/main/java/com/tools/kafkabrowser/controller/CollectionController.java`
- Modify: `kafka-browser/src/main/java/com/tools/kafkabrowser/repository/TemplateRepository.java`

**Step 1: Add query methods to TemplateRepository**

```java
// src/main/java/com/tools/kafkabrowser/repository/TemplateRepository.java
package com.tools.kafkabrowser.repository;

import com.tools.kafkabrowser.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findAllByOrderByUpdatedAtDesc();
    List<Template> findByCollectionIdOrderByUpdatedAtDesc(Long collectionId);
    List<Template> findByCollectionIsNullOrderByUpdatedAtDesc();
    int countByCollectionId(Long collectionId);
}
```

**Step 2: Update TemplateController**

```java
// src/main/java/com/tools/kafkabrowser/controller/TemplateController.java
package com.tools.kafkabrowser.controller;

import com.tools.kafkabrowser.model.Template;
import com.tools.kafkabrowser.model.TemplateDto;
import com.tools.kafkabrowser.model.TemplateSummaryDto;
import com.tools.kafkabrowser.repository.CollectionRepository;
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
    private final CollectionRepository collectionRepository;

    @GetMapping
    public List<TemplateSummaryDto> listTemplates(
            @RequestParam(required = false) Long collectionId) {
        List<Template> templates;
        if (collectionId != null) {
            templates = templateRepository.findByCollectionIdOrderByUpdatedAtDesc(collectionId);
        } else {
            templates = templateRepository.findAllByOrderByUpdatedAtDesc();
        }
        return templates.stream().map(TemplateSummaryDto::from).toList();
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
        template.setJsonContent(dto.jsonContent());
        if (dto.collectionId() != null) {
            var collection = collectionRepository.findById(dto.collectionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
            template.setCollection(collection);
        }
        Template saved = templateRepository.save(template);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Template updateTemplate(@PathVariable Long id, @RequestBody TemplateDto dto) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));
        template.setName(dto.name());
        template.setJsonContent(dto.jsonContent());
        if (dto.collectionId() != null) {
            var collection = collectionRepository.findById(dto.collectionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
            template.setCollection(collection);
        } else {
            template.setCollection(null);
        }
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

**Step 3: Create CollectionController**

```java
// src/main/java/com/tools/kafkabrowser/controller/CollectionController.java
package com.tools.kafkabrowser.controller;

import com.tools.kafkabrowser.model.*;
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
        // Delete all templates in the collection first
        List<Template> templates = templateRepository.findByCollectionIdOrderByUpdatedAtDesc(id);
        templateRepository.deleteAll(templates);
        collectionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<BulkProduceResponse> runCollection(@PathVariable Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));

        if (collection.getTopicName() == null || collection.getTopicName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection has no topic configured");
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
                var metadata = kafkaProducerService.send(
                        collection.getTopicName(),
                        collection.getSchemaSubject(),
                        template.getJsonContent()
                );
                results.add(new ProduceResponse(true, metadata.topic(), metadata.partition(), metadata.offset(), null));
                succeeded++;
            } catch (Exception e) {
                log.error("Failed to produce template '{}': {}", template.getName(), e.getMessage());
                results.add(new ProduceResponse(false, collection.getTopicName(), -1, -1, e.getMessage()));
                failed++;
            }
        }

        return ResponseEntity.ok(new BulkProduceResponse(templates.size(), succeeded, failed, results));
    }
}
```

**Step 4: Verify compilation**

Run: `cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser && ./mvnw compile -q`
Expected: BUILD SUCCESS

**Step 5: Test API manually**

Run backend:
```bash
cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Test collection CRUD:
```bash
# Create collection
curl -s -X POST http://localhost:8080/api/collections \
  -H 'Content-Type: application/json' \
  -d '{"name":"Payments","topicName":"payments","schemaSubject":null,"avscContent":null}'

# List collections
curl -s http://localhost:8080/api/collections

# Create template in collection
curl -s -X POST http://localhost:8080/api/templates \
  -H 'Content-Type: application/json' \
  -d '{"name":"test-tx","jsonContent":"{\"amount\":100}","collectionId":1}'

# List templates in collection
curl -s "http://localhost:8080/api/templates?collectionId=1"
```

**Step 6: Commit**

```bash
git add src/main/java/com/tools/kafkabrowser/model/ \
       src/main/java/com/tools/kafkabrowser/repository/ \
       src/main/java/com/tools/kafkabrowser/controller/
git commit -m "feat: add Collection entity, controller, and update Template model

- Collection entity with name, topicName, schemaSubject, avscContent
- CollectionController with full CRUD + run endpoint
- Template now has ManyToOne relationship to Collection
- Removed topicName/schemaSubject from Template (now on Collection)
- Updated TemplateController to accept collectionId filter
- Updated DTOs to match new schema"
```

---

## Task 5: Frontend Types & API Layer

**Files:**
- Modify: `kafka-browser/frontend/src/lib/types.ts`
- Modify: `kafka-browser/frontend/src/lib/api.ts`

**Step 1: Update types.ts — add Collection types, update Template types**

Add Collection types and update Template types. Remove `topicName`/`schemaSubject` from `TemplateSummary`, `TemplateDetail`, and `TemplateFormData`. Add `collectionId`.

In `types.ts`, replace the template-related interfaces:

```typescript
// Replace TemplateSummary
export interface TemplateSummary {
  id: number;
  name: string;
  collectionId: number | null;
  createdAt: string;
  updatedAt: string;
}

// Replace TemplateDetail
export interface TemplateDetail extends TemplateSummary {
  jsonContent: string;
}

// Replace TemplateFormData
export interface TemplateFormData {
  name: string;
  jsonContent: string;
  collectionId: number | null;
}
```

Add new Collection types after the template types:

```typescript
export interface CollectionSummary {
  id: number;
  name: string;
  topicName: string | null;
  schemaSubject: string | null;
  hasAvsc: boolean;
  templateCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CollectionDetail {
  id: number;
  name: string;
  topicName: string | null;
  schemaSubject: string | null;
  avscContent: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CollectionFormData {
  name: string;
  topicName: string | null;
  schemaSubject: string | null;
  avscContent: string | null;
}
```

**Step 2: Update api.ts — add collection functions, update template functions**

Update `createTemplate` and `updateTemplate` to use the new `TemplateFormData` shape (no more topicName/schemaSubject).

Add after the template section:

```typescript
// --- Collections ---

export async function fetchCollections(): Promise<CollectionSummary[]> {
  const res = await fetch("/api/collections");
  if (!res.ok) throw new Error(`Failed to fetch collections: ${res.status}`);
  return res.json();
}

export async function fetchCollection(id: number): Promise<CollectionDetail> {
  const res = await fetch(`/api/collections/${id}`);
  if (!res.ok) throw new Error(`Failed to fetch collection: ${res.status}`);
  return res.json();
}

export async function createCollection(data: CollectionFormData): Promise<CollectionDetail> {
  const res = await fetch("/api/collections", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error(`Failed to create collection: ${res.status}`);
  return res.json();
}

export async function updateCollection(id: number, data: CollectionFormData): Promise<CollectionDetail> {
  const res = await fetch(`/api/collections/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error(`Failed to update collection: ${res.status}`);
  return res.json();
}

export async function deleteCollection(id: number): Promise<void> {
  const res = await fetch(`/api/collections/${id}`, { method: "DELETE" });
  if (!res.ok) throw new Error(`Failed to delete collection: ${res.status}`);
}

export async function runCollection(id: number): Promise<BulkProduceResponse> {
  const res = await fetch(`/api/collections/${id}/run`, { method: "POST" });
  if (!res.ok) throw new Error(`Failed to run collection: ${res.status}`);
  return res.json();
}
```

Update imports at top of `api.ts` to include new types:

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
  CollectionSummary,
  CollectionDetail,
  CollectionFormData,
} from "./types";
```

Also update `fetchTemplates` to accept optional collectionId:

```typescript
export async function fetchTemplates(collectionId?: number): Promise<TemplateSummary[]> {
  const url = collectionId
    ? `/api/templates?collectionId=${collectionId}`
    : "/api/templates";
  const res = await fetch(url);
  if (!res.ok) throw new Error(`Failed to fetch templates: ${res.status}`);
  return res.json();
}
```

**Step 3: Verify TypeScript compilation**

Run: `cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser/frontend && npx tsc --noEmit 2>&1 | head -20`
Expected: Errors in components referencing old types (topicName, schemaSubject on templates). This is expected — will be fixed when components are replaced.

**Step 4: Commit**

```bash
cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser/frontend
git add src/lib/types.ts src/lib/api.ts
git commit -m "feat: add Collection types and API functions, update Template types

- CollectionSummary, CollectionDetail, CollectionFormData types
- CRUD + run API functions for collections
- Template types: removed topicName/schemaSubject, added collectionId
- fetchTemplates now accepts optional collectionId filter"
```

---

## Task 6: useCollections Hook

**Files:**
- Create: `kafka-browser/frontend/src/hooks/use-collections.ts`
- Modify: `kafka-browser/frontend/src/hooks/use-templates.ts`

**Step 1: Create useCollections hook**

```typescript
// src/hooks/use-collections.ts
"use client";

import { useState, useEffect, useCallback } from "react";
import {
  CollectionSummary,
  CollectionDetail,
  CollectionFormData,
  BulkProduceResponse,
} from "@/lib/types";
import {
  fetchCollections,
  fetchCollection,
  createCollection,
  updateCollection,
  deleteCollection,
  runCollection,
} from "@/lib/api";

export function useCollections() {
  const [collections, setCollections] = useState<CollectionSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchCollections();
      setCollections(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch collections");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const get = useCallback(async (id: number): Promise<CollectionDetail | null> => {
    try {
      return await fetchCollection(id);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch collection");
      return null;
    }
  }, []);

  const create = useCallback(async (data: CollectionFormData): Promise<CollectionDetail | null> => {
    try {
      const result = await createCollection(data);
      await refresh();
      return result;
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create collection");
      return null;
    }
  }, [refresh]);

  const update = useCallback(async (id: number, data: CollectionFormData): Promise<CollectionDetail | null> => {
    try {
      const result = await updateCollection(id, data);
      await refresh();
      return result;
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update collection");
      return null;
    }
  }, [refresh]);

  const remove = useCallback(async (id: number): Promise<boolean> => {
    try {
      await deleteCollection(id);
      await refresh();
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete collection");
      return false;
    }
  }, [refresh]);

  const run = useCallback(async (id: number): Promise<BulkProduceResponse | null> => {
    try {
      return await runCollection(id);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to run collection");
      return null;
    }
  }, []);

  return { collections, loading, error, refresh, get, create, update, remove, run };
}
```

**Step 2: Update useTemplates to accept collectionId**

Update `useTemplates` in `src/hooks/use-templates.ts` to accept an optional `collectionId` parameter and re-fetch when it changes:

```typescript
"use client";

import { useState, useEffect, useCallback } from "react";
import { TemplateSummary, TemplateDetail, TemplateFormData } from "@/lib/types";
import {
  fetchTemplates,
  fetchTemplate,
  createTemplate,
  updateTemplate,
  deleteTemplate,
} from "@/lib/api";

export function useTemplates(collectionId?: number | null) {
  const [templates, setTemplates] = useState<TemplateSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchTemplates(collectionId ?? undefined);
      setTemplates(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch templates");
    } finally {
      setLoading(false);
    }
  }, [collectionId]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const get = useCallback(async (id: number): Promise<TemplateDetail | null> => {
    try {
      return await fetchTemplate(id);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch template");
      return null;
    }
  }, []);

  const create = useCallback(async (data: TemplateFormData): Promise<TemplateDetail | null> => {
    try {
      const result = await createTemplate(data);
      await refresh();
      return result;
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create template");
      return null;
    }
  }, [refresh]);

  const update = useCallback(async (id: number, data: TemplateFormData): Promise<TemplateDetail | null> => {
    try {
      const result = await updateTemplate(id, data);
      await refresh();
      return result;
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update template");
      return null;
    }
  }, [refresh]);

  const remove = useCallback(async (id: number): Promise<boolean> => {
    try {
      await deleteTemplate(id);
      await refresh();
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete template");
      return false;
    }
  }, [refresh]);

  return { templates, loading, error, refresh, get, create, update, remove };
}
```

**Step 3: Commit**

```bash
cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser/frontend
git add src/hooks/use-collections.ts src/hooks/use-templates.ts
git commit -m "feat: add useCollections hook, update useTemplates with collectionId filter"
```

---

## Task 7: ConfirmSendDialog Component

**Files:**
- Create: `kafka-browser/frontend/src/components/confirm-send-dialog.tsx`

**Step 1: Create ConfirmSendDialog**

```typescript
// src/components/confirm-send-dialog.tsx
"use client";

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

interface ConfirmSendDialogProps {
  open: boolean;
  topic: string;
  schemaSubject: string | null;
  messageCount: number;
  onConfirm: () => void;
  onCancel: () => void;
  sending?: boolean;
}

export function ConfirmSendDialog({
  open,
  topic,
  schemaSubject,
  messageCount,
  onConfirm,
  onCancel,
  sending = false,
}: ConfirmSendDialogProps) {
  return (
    <Dialog open={open} onOpenChange={(isOpen) => !isOpen && onCancel()}>
      <DialogContent className="sm:max-w-[400px]">
        <DialogHeader>
          <DialogTitle>Confirm Send</DialogTitle>
          <DialogDescription>
            You are about to produce messages to Kafka.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-2 py-4 text-sm">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Topic</span>
            <span className="font-mono">{topic}</span>
          </div>
          {schemaSubject && (
            <div className="flex justify-between">
              <span className="text-muted-foreground">Schema</span>
              <span className="font-mono">{schemaSubject}</span>
            </div>
          )}
          <div className="flex justify-between">
            <span className="text-muted-foreground">Messages</span>
            <span>{messageCount} {messageCount === 1 ? "message" : "messages"}</span>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onCancel} disabled={sending}>
            Cancel
          </Button>
          <Button onClick={onConfirm} disabled={sending}>
            {sending ? "Sending..." : "Confirm Send"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
```

**Step 2: Commit**

```bash
cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser/frontend
git add src/components/confirm-send-dialog.tsx
git commit -m "feat: add ConfirmSendDialog component"
```

---

## Task 8: CollectionSidebar Component (Panel 1)

**Files:**
- Create: `kafka-browser/frontend/src/components/collection-sidebar.tsx`

**Step 1: Create CollectionSidebar**

```typescript
// src/components/collection-sidebar.tsx
"use client";

import { useState } from "react";
import { cn } from "@/lib/utils";
import { CollectionSummary } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Skeleton } from "@/components/ui/skeleton";
import { Plus, FolderOpen, Folder, Trash2, Upload } from "lucide-react";

interface CollectionSidebarProps {
  collections: CollectionSummary[];
  loading: boolean;
  selectedId: number | null;
  onSelect: (id: number) => void;
  onCreate: (name: string) => void;
  onDelete: (id: number) => void;
  onUploadAvsc: (id: number, content: string) => void;
}

export function CollectionSidebar({
  collections,
  loading,
  selectedId,
  onSelect,
  onCreate,
  onDelete,
  onUploadAvsc,
}: CollectionSidebarProps) {
  const [creating, setCreating] = useState(false);
  const [newName, setNewName] = useState("");

  const handleCreate = () => {
    if (newName.trim()) {
      onCreate(newName.trim());
      setNewName("");
      setCreating(false);
    }
  };

  const handleAvscUpload = (collectionId: number) => {
    const input = document.createElement("input");
    input.type = "file";
    input.accept = ".avsc,.json";
    input.onchange = async (e) => {
      const file = (e.target as HTMLInputElement).files?.[0];
      if (file) {
        const content = await file.text();
        onUploadAvsc(collectionId, content);
      }
    };
    input.click();
  };

  return (
    <div className="flex flex-col h-full border-r border-white/[0.06] w-[180px] shrink-0">
      <div className="flex items-center justify-between px-3 h-10 border-b border-white/[0.06] shrink-0">
        <span className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
          Collections
        </span>
        <Button
          variant="ghost"
          size="icon"
          className="h-6 w-6"
          onClick={() => setCreating(true)}
        >
          <Plus className="h-3.5 w-3.5" />
        </Button>
      </div>

      <ScrollArea className="flex-1">
        <div className="p-1.5">
          {loading && (
            <div className="space-y-1">
              {Array.from({ length: 4 }).map((_, i) => (
                <Skeleton key={i} className="h-8 w-full rounded-md" />
              ))}
            </div>
          )}

          {!loading && collections.map((col) => {
            const isSelected = col.id === selectedId;
            return (
              <div
                key={col.id}
                className={cn(
                  "group flex items-center gap-1.5 px-2 py-1.5 rounded-md cursor-pointer text-sm transition-colors",
                  isSelected
                    ? "bg-white/[0.08] text-teal-400"
                    : "text-muted-foreground hover:text-foreground hover:bg-white/[0.04]"
                )}
                onClick={() => onSelect(col.id)}
              >
                {isSelected ? (
                  <FolderOpen className="h-3.5 w-3.5 shrink-0" />
                ) : (
                  <Folder className="h-3.5 w-3.5 shrink-0" />
                )}
                <span className="truncate flex-1">{col.name}</span>
                <span className="text-[10px] text-muted-foreground/60">{col.templateCount}</span>
                <div className="hidden group-hover:flex items-center gap-0.5">
                  <button
                    className="p-0.5 rounded hover:bg-white/[0.08]"
                    onClick={(e) => { e.stopPropagation(); handleAvscUpload(col.id); }}
                    title="Upload .avsc"
                  >
                    <Upload className="h-3 w-3" />
                  </button>
                  <button
                    className="p-0.5 rounded hover:bg-red-500/20 text-red-400"
                    onClick={(e) => { e.stopPropagation(); onDelete(col.id); }}
                    title="Delete collection"
                  >
                    <Trash2 className="h-3 w-3" />
                  </button>
                </div>
              </div>
            );
          })}

          {creating && (
            <div className="p-1">
              <Input
                autoFocus
                placeholder="Collection name"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") handleCreate();
                  if (e.key === "Escape") { setCreating(false); setNewName(""); }
                }}
                onBlur={() => { if (!newName.trim()) setCreating(false); }}
                className="h-7 text-xs"
              />
            </div>
          )}
        </div>
      </ScrollArea>
    </div>
  );
}
```

**Step 2: Commit**

```bash
cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser/frontend
git add src/components/collection-sidebar.tsx
git commit -m "feat: add CollectionSidebar component (Panel 1)"
```

---

## Task 9: TemplateListPanel Component (Panel 2)

**Files:**
- Create: `kafka-browser/frontend/src/components/template-list-panel.tsx`

**Step 1: Create TemplateListPanel**

```typescript
// src/components/template-list-panel.tsx
"use client";

import { useRef } from "react";
import { cn } from "@/lib/utils";
import { TemplateSummary, CollectionDetail } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Skeleton } from "@/components/ui/skeleton";
import { Plus, Play, Trash2, FileJson } from "lucide-react";

interface TemplateListPanelProps {
  templates: TemplateSummary[];
  loading: boolean;
  selectedId: number | null;
  collection: CollectionDetail | null;
  subjects: string[];
  onSelect: (id: number) => void;
  onUpload: (name: string, content: string) => void;
  onDelete: (id: number) => void;
  onRunAll: () => void;
  onTopicChange: (topic: string) => void;
  onSchemaChange: (schema: string) => void;
}

export function TemplateListPanel({
  templates,
  loading,
  selectedId,
  collection,
  subjects,
  onSelect,
  onUpload,
  onDelete,
  onRunAll,
  onTopicChange,
  onSchemaChange,
}: TemplateListPanelProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;
    for (const file of Array.from(files)) {
      const content = await file.text();
      const name = file.name.replace(/\.json$/i, "");
      onUpload(name, content);
    }
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  return (
    <div className="flex flex-col h-full border-r border-white/[0.06] w-[240px] shrink-0">
      <div className="flex items-center justify-between px-3 h-10 border-b border-white/[0.06] shrink-0">
        <span className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
          Templates
        </span>
        <Button
          variant="ghost"
          size="icon"
          className="h-6 w-6"
          onClick={() => fileInputRef.current?.click()}
        >
          <Plus className="h-3.5 w-3.5" />
        </Button>
        <input
          ref={fileInputRef}
          type="file"
          accept=".json"
          multiple
          className="hidden"
          onChange={handleFileUpload}
        />
      </div>

      <ScrollArea className="flex-1">
        <div className="p-1.5">
          {loading && (
            <div className="space-y-1">
              {Array.from({ length: 5 }).map((_, i) => (
                <Skeleton key={i} className="h-8 w-full rounded-md" />
              ))}
            </div>
          )}

          {!loading && templates.length === 0 && (
            <div className="text-xs text-muted-foreground/60 text-center py-8 px-2">
              No templates yet. Click + to upload JSON files.
            </div>
          )}

          {!loading && templates.map((t) => {
            const isSelected = t.id === selectedId;
            return (
              <div
                key={t.id}
                className={cn(
                  "group flex items-center gap-1.5 px-2 py-1.5 rounded-md cursor-pointer text-sm transition-colors",
                  isSelected
                    ? "bg-white/[0.08] text-teal-400"
                    : "text-muted-foreground hover:text-foreground hover:bg-white/[0.04]"
                )}
                onClick={() => onSelect(t.id)}
              >
                <FileJson className="h-3.5 w-3.5 shrink-0" />
                <span className="truncate flex-1">{t.name}</span>
                <button
                  className="hidden group-hover:block p-0.5 rounded hover:bg-red-500/20 text-red-400"
                  onClick={(e) => { e.stopPropagation(); onDelete(t.id); }}
                >
                  <Trash2 className="h-3 w-3" />
                </button>
              </div>
            );
          })}
        </div>
      </ScrollArea>

      {/* Collection metadata section */}
      {collection && (
        <div className="border-t border-white/[0.06] p-3 space-y-2 shrink-0">
          <div>
            <label className="text-[10px] uppercase tracking-wider text-muted-foreground/60">
              Topic
            </label>
            <Input
              value={collection.topicName || ""}
              onChange={(e) => onTopicChange(e.target.value)}
              placeholder="topic-name"
              className="h-7 text-xs mt-0.5"
            />
          </div>
          <div>
            <label className="text-[10px] uppercase tracking-wider text-muted-foreground/60">
              Schema Subject
            </label>
            <Input
              value={collection.schemaSubject || ""}
              onChange={(e) => onSchemaChange(e.target.value)}
              placeholder="schema-subject"
              list="schema-subjects"
              className="h-7 text-xs mt-0.5"
            />
            <datalist id="schema-subjects">
              {subjects.map((s) => (
                <option key={s} value={s} />
              ))}
            </datalist>
          </div>
          <Button
            size="sm"
            className="w-full h-7 text-xs"
            onClick={onRunAll}
            disabled={!collection.topicName || templates.length === 0}
          >
            <Play className="h-3 w-3 mr-1.5" />
            Run All ({templates.length})
          </Button>
        </div>
      )}
    </div>
  );
}
```

**Step 2: Commit**

```bash
cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser/frontend
git add src/components/template-list-panel.tsx
git commit -m "feat: add TemplateListPanel component (Panel 2)"
```

---

## Task 10: FormEditor Component (Panel 3)

**Files:**
- Create: `kafka-browser/frontend/src/components/form-editor.tsx`
- Create: `kafka-browser/frontend/src/components/form-field-renderer.tsx`

**Step 1: Create FormFieldRenderer — recursive form field component**

```typescript
// src/components/form-field-renderer.tsx
"use client";

import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { ChevronDown, ChevronRight, Plus, Trash2 } from "lucide-react";
import { cn } from "@/lib/utils";

interface FormFieldRendererProps {
  label: string;
  value: unknown;
  depth: number;
  onChange: (value: unknown) => void;
  onDelete?: () => void;
}

export function FormFieldRenderer({
  label,
  value,
  depth,
  onChange,
  onDelete,
}: FormFieldRendererProps) {
  const [expanded, setExpanded] = useState(depth < 2);

  if (value !== null && typeof value === "object" && !Array.isArray(value)) {
    const obj = value as Record<string, unknown>;
    const keys = Object.keys(obj);
    return (
      <div className={cn("border-l border-white/[0.06]", depth > 0 && "ml-3")}>
        <div
          className="group flex items-center gap-1 py-1 px-1 cursor-pointer hover:bg-white/[0.02] rounded-sm"
          onClick={() => setExpanded(!expanded)}
        >
          {expanded ? (
            <ChevronDown className="h-3 w-3 text-muted-foreground shrink-0" />
          ) : (
            <ChevronRight className="h-3 w-3 text-muted-foreground shrink-0" />
          )}
          <span className="text-xs font-medium text-muted-foreground">{label}</span>
          <span className="text-[10px] text-muted-foreground/40">{`{${keys.length}}`}</span>
          {onDelete && (
            <button
              className="hidden group-hover:block ml-auto p-0.5 rounded hover:bg-red-500/20 text-red-400"
              onClick={(e) => { e.stopPropagation(); onDelete(); }}
            >
              <Trash2 className="h-3 w-3" />
            </button>
          )}
        </div>
        {expanded && (
          <div className="pl-2">
            {keys.map((key) => (
              <FormFieldRenderer
                key={key}
                label={key}
                value={obj[key]}
                depth={depth + 1}
                onChange={(v) => onChange({ ...obj, [key]: v })}
                onDelete={() => {
                  const next = { ...obj };
                  delete next[key];
                  onChange(next);
                }}
              />
            ))}
            <Button
              variant="ghost"
              size="sm"
              className="h-6 text-[10px] text-muted-foreground mt-1"
              onClick={() => {
                const key = `newField${keys.length}`;
                onChange({ ...obj, [key]: "" });
              }}
            >
              <Plus className="h-3 w-3 mr-1" /> Add Field
            </Button>
          </div>
        )}
      </div>
    );
  }

  if (Array.isArray(value)) {
    return (
      <div className={cn("border-l border-white/[0.06]", depth > 0 && "ml-3")}>
        <div
          className="group flex items-center gap-1 py-1 px-1 cursor-pointer hover:bg-white/[0.02] rounded-sm"
          onClick={() => setExpanded(!expanded)}
        >
          {expanded ? (
            <ChevronDown className="h-3 w-3 text-muted-foreground shrink-0" />
          ) : (
            <ChevronRight className="h-3 w-3 text-muted-foreground shrink-0" />
          )}
          <span className="text-xs font-medium text-muted-foreground">{label}</span>
          <span className="text-[10px] text-muted-foreground/40">{`[${value.length}]`}</span>
          {onDelete && (
            <button
              className="hidden group-hover:block ml-auto p-0.5 rounded hover:bg-red-500/20 text-red-400"
              onClick={(e) => { e.stopPropagation(); onDelete(); }}
            >
              <Trash2 className="h-3 w-3" />
            </button>
          )}
        </div>
        {expanded && (
          <div className="pl-2">
            {value.map((item, i) => (
              <FormFieldRenderer
                key={i}
                label={`[${i}]`}
                value={item}
                depth={depth + 1}
                onChange={(v) => {
                  const next = [...value];
                  next[i] = v;
                  onChange(next);
                }}
                onDelete={() => {
                  const next = value.filter((_, idx) => idx !== i);
                  onChange(next);
                }}
              />
            ))}
            <Button
              variant="ghost"
              size="sm"
              className="h-6 text-[10px] text-muted-foreground mt-1"
              onClick={() => onChange([...value, ""])}
            >
              <Plus className="h-3 w-3 mr-1" /> Add Item
            </Button>
          </div>
        )}
      </div>
    );
  }

  // Scalar value
  return (
    <div className={cn("group flex items-center gap-2 py-0.5 px-1", depth > 0 && "ml-3")}>
      <label className="text-xs text-muted-foreground w-28 shrink-0 truncate" title={label}>
        {label}
      </label>
      {typeof value === "boolean" ? (
        <input
          type="checkbox"
          checked={value}
          onChange={(e) => onChange(e.target.checked)}
          className="h-3.5 w-3.5 accent-teal-500"
        />
      ) : typeof value === "number" ? (
        <Input
          type="number"
          value={value}
          onChange={(e) => {
            const v = e.target.value;
            onChange(v === "" ? 0 : Number(v));
          }}
          className="h-7 text-xs flex-1"
        />
      ) : (
        <Input
          value={value === null ? "" : String(value)}
          onChange={(e) => onChange(e.target.value)}
          placeholder="(empty)"
          className="h-7 text-xs flex-1"
        />
      )}
      {onDelete && (
        <button
          className="hidden group-hover:block p-0.5 rounded hover:bg-red-500/20 text-red-400 shrink-0"
          onClick={onDelete}
        >
          <Trash2 className="h-3 w-3" />
        </button>
      )}
    </div>
  );
}
```

**Step 2: Create FormEditor**

```typescript
// src/components/form-editor.tsx
"use client";

import { useState, useMemo } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { FormFieldRenderer } from "@/components/form-field-renderer";
import { Save, Send, ToggleLeft, ToggleRight } from "lucide-react";

interface FormEditorProps {
  name: string;
  jsonContent: string;
  onNameChange: (name: string) => void;
  onJsonChange: (json: string) => void;
  onSave: () => void;
  onSend: () => void;
  saving: boolean;
  sendDisabled: boolean;
  sendResult?: { success: boolean; message: string } | null;
}

export function FormEditor({
  name,
  jsonContent,
  onNameChange,
  onJsonChange,
  onSave,
  onSend,
  saving,
  sendDisabled,
  sendResult,
}: FormEditorProps) {
  const [viewMode, setViewMode] = useState<"form" | "raw">("form");
  const [rawContent, setRawContent] = useState(jsonContent);
  const [parseError, setParseError] = useState<string | null>(null);

  const parsedData = useMemo(() => {
    try {
      const data = JSON.parse(jsonContent);
      setParseError(null);
      return data;
    } catch (e) {
      setParseError(e instanceof Error ? e.message : "Invalid JSON");
      return null;
    }
  }, [jsonContent]);

  const handleFormChange = (value: unknown) => {
    const json = JSON.stringify(value, null, 2);
    onJsonChange(json);
    setRawContent(json);
  };

  const handleRawChange = (value: string) => {
    setRawContent(value);
    try {
      JSON.parse(value);
      onJsonChange(value);
      setParseError(null);
    } catch (e) {
      setParseError(e instanceof Error ? e.message : "Invalid JSON");
    }
  };

  const handleModeSwitch = (mode: "form" | "raw") => {
    if (mode === "raw") {
      setRawContent(jsonContent);
    } else if (mode === "form") {
      try {
        JSON.parse(rawContent);
        onJsonChange(rawContent);
      } catch {
        // stay in raw mode if JSON is invalid
        return;
      }
    }
    setViewMode(mode);
  };

  return (
    <div className="flex flex-col h-full flex-1">
      {/* Header */}
      <div className="flex items-center gap-2 px-4 h-10 border-b border-white/[0.06] shrink-0">
        <Input
          value={name}
          onChange={(e) => onNameChange(e.target.value)}
          placeholder="Template name"
          className="h-7 text-xs w-48"
        />
        <div className="flex-1" />
        <div className="flex items-center gap-1 text-xs text-muted-foreground">
          <button
            className={viewMode === "form" ? "text-teal-400 font-medium" : "hover:text-foreground"}
            onClick={() => handleModeSwitch("form")}
          >
            Form
          </button>
          <span className="mx-1">|</span>
          <button
            className={viewMode === "raw" ? "text-teal-400 font-medium" : "hover:text-foreground"}
            onClick={() => handleModeSwitch("raw")}
          >
            Raw JSON
          </button>
        </div>
      </div>

      {/* Content */}
      <ScrollArea className="flex-1">
        <div className="p-4">
          {viewMode === "form" && parsedData !== null && (
            <div className="space-y-0.5">
              {typeof parsedData === "object" && !Array.isArray(parsedData)
                ? Object.keys(parsedData).map((key) => (
                    <FormFieldRenderer
                      key={key}
                      label={key}
                      value={parsedData[key]}
                      depth={0}
                      onChange={(v) => handleFormChange({ ...parsedData, [key]: v })}
                      onDelete={() => {
                        const next = { ...parsedData };
                        delete next[key];
                        handleFormChange(next);
                      }}
                    />
                  ))
                : (
                  <FormFieldRenderer
                    label="root"
                    value={parsedData}
                    depth={0}
                    onChange={handleFormChange}
                  />
                )}
              <Button
                variant="ghost"
                size="sm"
                className="h-7 text-xs text-muted-foreground mt-2"
                onClick={() => {
                  if (typeof parsedData === "object" && !Array.isArray(parsedData)) {
                    const key = `newField${Object.keys(parsedData).length}`;
                    handleFormChange({ ...parsedData, [key]: "" });
                  }
                }}
              >
                + Add Field
              </Button>
            </div>
          )}

          {viewMode === "form" && parsedData === null && (
            <div className="text-xs text-red-400 py-4">
              Cannot render form: {parseError}
            </div>
          )}

          {viewMode === "raw" && (
            <textarea
              value={rawContent}
              onChange={(e) => handleRawChange(e.target.value)}
              className="w-full h-[500px] bg-transparent text-xs font-mono resize-none focus:outline-none text-foreground"
              spellCheck={false}
            />
          )}

          {parseError && viewMode === "raw" && (
            <div className="text-[10px] text-red-400 mt-1">{parseError}</div>
          )}
        </div>
      </ScrollArea>

      {/* Footer */}
      <div className="flex items-center gap-2 px-4 h-12 border-t border-white/[0.06] shrink-0">
        <Button size="sm" variant="outline" onClick={onSave} disabled={saving || !name.trim()}>
          <Save className="h-3.5 w-3.5 mr-1.5" />
          {saving ? "Saving..." : "Save"}
        </Button>
        <Button size="sm" onClick={onSend} disabled={sendDisabled}>
          <Send className="h-3.5 w-3.5 mr-1.5" />
          Send to Kafka
        </Button>
        {sendResult && (
          <span className={`text-xs ${sendResult.success ? "text-emerald-400" : "text-red-400"}`}>
            {sendResult.message}
          </span>
        )}
      </div>
    </div>
  );
}
```

**Step 3: Commit**

```bash
cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser/frontend
git add src/components/form-editor.tsx src/components/form-field-renderer.tsx
git commit -m "feat: add FormEditor and FormFieldRenderer components (Panel 3)"
```

---

## Task 11: Producer Page — Three-Panel Layout

**Files:**
- Create: `kafka-browser/frontend/src/app/producer/page.tsx`
- Delete (later): `kafka-browser/frontend/src/app/templates/page.tsx`

**Step 1: Create the Producer page with three-panel layout**

```typescript
// src/app/producer/page.tsx
"use client";

import { useState, useCallback, useEffect } from "react";
import { useCollections } from "@/hooks/use-collections";
import { useTemplates } from "@/hooks/use-templates";
import { useProducer } from "@/hooks/use-producer";
import { useSchemas } from "@/hooks/use-schemas";
import { CollectionSidebar } from "@/components/collection-sidebar";
import { TemplateListPanel } from "@/components/template-list-panel";
import { FormEditor } from "@/components/form-editor";
import { ConfirmSendDialog } from "@/components/confirm-send-dialog";
import { CollectionDetail } from "@/lib/types";
import { fetchCollection, updateCollection } from "@/lib/api";

export default function ProducerPage() {
  // Collections
  const collections = useCollections();
  const [selectedCollectionId, setSelectedCollectionId] = useState<number | null>(null);
  const [collectionDetail, setCollectionDetail] = useState<CollectionDetail | null>(null);

  // Templates (filtered by collection)
  const templates = useTemplates(selectedCollectionId);
  const [selectedTemplateId, setSelectedTemplateId] = useState<number | null>(null);

  // Form state
  const [name, setName] = useState("");
  const [jsonContent, setJsonContent] = useState("");
  const [saving, setSaving] = useState(false);
  const [sendResult, setSendResult] = useState<{ success: boolean; message: string } | null>(null);

  // Producer
  const producer = useProducer();
  const schemas = useSchemas();

  // Confirm dialog
  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    topic: string;
    schema: string | null;
    count: number;
    mode: "single" | "collection";
  }>({ open: false, topic: "", schema: null, count: 0, mode: "single" });

  // Load collection detail when selected
  useEffect(() => {
    if (selectedCollectionId) {
      fetchCollection(selectedCollectionId).then(setCollectionDetail).catch(() => setCollectionDetail(null));
    } else {
      setCollectionDetail(null);
    }
    setSelectedTemplateId(null);
    setName("");
    setJsonContent("");
    setSendResult(null);
  }, [selectedCollectionId]);

  // Load template when selected
  const handleSelectTemplate = useCallback(async (id: number) => {
    setSelectedTemplateId(id);
    setSendResult(null);
    const detail = await templates.get(id);
    if (detail) {
      setName(detail.name);
      setJsonContent(detail.jsonContent || "{}");
    }
  }, [templates]);

  // Create collection
  const handleCreateCollection = useCallback(async (collName: string) => {
    const result = await collections.create({
      name: collName,
      topicName: null,
      schemaSubject: null,
      avscContent: null,
    });
    if (result) setSelectedCollectionId(result.id);
  }, [collections]);

  // Upload .avsc to collection
  const handleUploadAvsc = useCallback(async (collId: number, content: string) => {
    const detail = await collections.get(collId);
    if (detail) {
      await collections.update(collId, {
        name: detail.name,
        topicName: detail.topicName,
        schemaSubject: detail.schemaSubject,
        avscContent: content,
      });
      if (collId === selectedCollectionId) {
        const updated = await fetchCollection(collId);
        setCollectionDetail(updated);
      }
    }
  }, [collections, selectedCollectionId]);

  // Upload template JSON
  const handleUploadTemplate = useCallback(async (fileName: string, content: string) => {
    await templates.create({
      name: fileName,
      jsonContent: content,
      collectionId: selectedCollectionId,
    });
  }, [templates, selectedCollectionId]);

  // Save template
  const handleSave = useCallback(async () => {
    if (!name.trim()) return;
    setSaving(true);
    try {
      if (selectedTemplateId) {
        await templates.update(selectedTemplateId, {
          name,
          jsonContent,
          collectionId: selectedCollectionId,
        });
      } else {
        const result = await templates.create({
          name,
          jsonContent,
          collectionId: selectedCollectionId,
        });
        if (result) setSelectedTemplateId(result.id);
      }
      setSendResult({ success: true, message: "Saved" });
    } catch {
      setSendResult({ success: false, message: "Failed to save" });
    } finally {
      setSaving(false);
    }
  }, [name, jsonContent, selectedTemplateId, selectedCollectionId, templates]);

  // Send single template — open confirm dialog
  const handleSendSingle = useCallback(() => {
    if (!collectionDetail?.topicName) return;
    setConfirmDialog({
      open: true,
      topic: collectionDetail.topicName,
      schema: collectionDetail.schemaSubject,
      count: 1,
      mode: "single",
    });
  }, [collectionDetail]);

  // Run all — open confirm dialog
  const handleRunAll = useCallback(() => {
    if (!collectionDetail?.topicName) return;
    setConfirmDialog({
      open: true,
      topic: collectionDetail.topicName,
      schema: collectionDetail.schemaSubject,
      count: templates.templates.length,
      mode: "collection",
    });
  }, [collectionDetail, templates.templates.length]);

  // Confirm send
  const handleConfirmSend = useCallback(async () => {
    if (confirmDialog.mode === "single") {
      const result = await producer.send(
        confirmDialog.topic,
        jsonContent,
        confirmDialog.schema || undefined,
      );
      setSendResult(
        result
          ? { success: result.success, message: result.success ? `Sent to partition ${result.partition} offset ${result.offset}` : result.error || "Failed" }
          : { success: false, message: "Failed to send" }
      );
    } else if (confirmDialog.mode === "collection" && selectedCollectionId) {
      const result = await collections.run(selectedCollectionId);
      setSendResult(
        result
          ? { success: result.failed === 0, message: `${result.succeeded}/${result.total} sent` }
          : { success: false, message: "Failed to run collection" }
      );
    }
    setConfirmDialog((prev) => ({ ...prev, open: false }));
  }, [confirmDialog, jsonContent, producer, collections, selectedCollectionId]);

  // Update collection topic/schema inline
  const handleTopicChange = useCallback(async (topic: string) => {
    if (!collectionDetail || !selectedCollectionId) return;
    const updated = await updateCollection(selectedCollectionId, {
      name: collectionDetail.name,
      topicName: topic,
      schemaSubject: collectionDetail.schemaSubject,
      avscContent: collectionDetail.avscContent,
    });
    setCollectionDetail(updated);
    collections.refresh();
  }, [collectionDetail, selectedCollectionId, collections]);

  const handleSchemaChange = useCallback(async (schema: string) => {
    if (!collectionDetail || !selectedCollectionId) return;
    const updated = await updateCollection(selectedCollectionId, {
      name: collectionDetail.name,
      topicName: collectionDetail.topicName,
      schemaSubject: schema,
      avscContent: collectionDetail.avscContent,
    });
    setCollectionDetail(updated);
    collections.refresh();
  }, [collectionDetail, selectedCollectionId, collections]);

  const hasContent = selectedTemplateId !== null || jsonContent.length > 2;

  return (
    <div className="flex h-full overflow-hidden">
      {/* Panel 1: Collections */}
      <CollectionSidebar
        collections={collections.collections}
        loading={collections.loading}
        selectedId={selectedCollectionId}
        onSelect={setSelectedCollectionId}
        onCreate={handleCreateCollection}
        onDelete={async (id) => {
          await collections.remove(id);
          if (id === selectedCollectionId) {
            setSelectedCollectionId(null);
          }
        }}
        onUploadAvsc={handleUploadAvsc}
      />

      {/* Panel 2: Templates */}
      {selectedCollectionId && (
        <TemplateListPanel
          templates={templates.templates}
          loading={templates.loading}
          selectedId={selectedTemplateId}
          collection={collectionDetail}
          subjects={schemas.subjects}
          onSelect={handleSelectTemplate}
          onUpload={handleUploadTemplate}
          onDelete={async (id) => {
            await templates.remove(id);
            if (id === selectedTemplateId) {
              setSelectedTemplateId(null);
              setName("");
              setJsonContent("");
            }
          }}
          onRunAll={handleRunAll}
          onTopicChange={handleTopicChange}
          onSchemaChange={handleSchemaChange}
        />
      )}

      {/* Panel 3: Form Editor */}
      {selectedCollectionId && hasContent ? (
        <FormEditor
          name={name}
          jsonContent={jsonContent}
          onNameChange={setName}
          onJsonChange={setJsonContent}
          onSave={handleSave}
          onSend={handleSendSingle}
          saving={saving}
          sendDisabled={!collectionDetail?.topicName || !jsonContent.trim()}
          sendResult={sendResult}
        />
      ) : (
        <div className="flex-1 flex items-center justify-center text-sm text-muted-foreground/40">
          {selectedCollectionId
            ? "Select a template or upload a JSON file"
            : "Select a collection to get started"}
        </div>
      )}

      {/* Confirm Dialog */}
      <ConfirmSendDialog
        open={confirmDialog.open}
        topic={confirmDialog.topic}
        schemaSubject={confirmDialog.schema}
        messageCount={confirmDialog.count}
        onConfirm={handleConfirmSend}
        onCancel={() => setConfirmDialog((prev) => ({ ...prev, open: false }))}
        sending={producer.sending}
      />
    </div>
  );
}
```

**Step 2: Commit**

```bash
cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser/frontend
git add src/app/producer/page.tsx
git commit -m "feat: add Producer page with three-panel layout"
```

---

## Task 12: Update NavBar & Route

**Files:**
- Modify: `kafka-browser/frontend/src/components/nav-bar.tsx`
- Delete: `kafka-browser/frontend/src/app/templates/page.tsx`

**Step 1: Update NavBar — rename Templates to Producer, update route**

In `nav-bar.tsx`, change the navItems array:

```typescript
const navItems = [
  { href: "/browser", label: "Browser", icon: Database },
  { href: "/producer", label: "Producer", icon: FileJson },
];
```

**Step 2: Remove old templates page**

Delete `src/app/templates/page.tsx` (the new Producer page at `src/app/producer/page.tsx` replaces it).

**Step 3: Remove old components that are replaced**

Delete these files (replaced by new components):
- `src/components/template-list.tsx` → replaced by `collection-sidebar.tsx` + `template-list-panel.tsx`
- `src/components/template-editor.tsx` → replaced by `form-editor.tsx`
- `src/components/editable-json-tree.tsx` → replaced by `form-field-renderer.tsx`
- `src/components/bulk-send-dialog.tsx` → replaced by collection run + `confirm-send-dialog.tsx`

**Step 4: Verify build**

Run: `cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser/frontend && npx next build`
Expected: Build succeeds, `/producer` route compiles.

**Step 5: Commit**

```bash
cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser/frontend
git rm src/app/templates/page.tsx
git rm src/components/template-list.tsx
git rm src/components/template-editor.tsx
git rm src/components/editable-json-tree.tsx
git rm src/components/bulk-send-dialog.tsx
git add src/components/nav-bar.tsx
git commit -m "feat: rename Templates to Producer, remove old components

- NavBar: Templates → Producer, route /templates → /producer
- Removed template-list.tsx (replaced by collection-sidebar + template-list-panel)
- Removed template-editor.tsx (replaced by form-editor)
- Removed editable-json-tree.tsx (replaced by form-field-renderer)
- Removed bulk-send-dialog.tsx (replaced by collection run + confirm-send-dialog)
- Removed templates/page.tsx (replaced by producer/page.tsx)"
```

---

## Task 13: End-to-End Manual Testing

**Step 1: Start backend**

```bash
cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Step 2: Start frontend**

```bash
cd /Users/cristian-robertiosef/Desktop/Dev/abcfw/kafka-browser/frontend && npm run dev
```

**Step 3: Test the following flows in browser at http://localhost:3000/producer**

1. **Collections**: Create a collection, verify it appears in sidebar
2. **Upload**: Upload a JSON template file, verify it appears in template list
3. **Form editing**: Click a template, verify form fields render from JSON
4. **Form/Raw toggle**: Switch between Form and Raw JSON views, verify bidirectional sync
5. **Save**: Edit a field, click Save, verify changes persist
6. **Collection metadata**: Set topic name and schema subject on collection
7. **Send single**: Click "Send to Kafka" on a template, verify confirm dialog
8. **Run all**: Click "Run All" button, verify confirm dialog with template count
9. **Delete**: Delete a template and a collection, verify cleanup
10. **Upload .avsc**: Upload .avsc file to collection via sidebar button

**Step 4: Fix any issues found during testing**

**Step 5: Final commit if any fixes were needed**

```bash
git add -A
git commit -m "fix: address issues from manual testing"
```

---

## Summary

| Task | What | Files |
|------|------|-------|
| 1 | Collection entity + repository | 2 new Java files |
| 2 | Template entity migration | 1 modified Java file |
| 3 | DTO updates + new DTOs | 4 Java files (2 modified, 2 new) |
| 4 | Controllers (Template update + Collection new) | 3 Java files (2 modified, 1 new) |
| 5 | Frontend types + API layer | 2 modified TS files |
| 6 | useCollections hook + useTemplates update | 1 new + 1 modified TS file |
| 7 | ConfirmSendDialog | 1 new TSX file |
| 8 | CollectionSidebar (Panel 1) | 1 new TSX file |
| 9 | TemplateListPanel (Panel 2) | 1 new TSX file |
| 10 | FormEditor + FormFieldRenderer (Panel 3) | 2 new TSX files |
| 11 | Producer page (three-panel layout) | 1 new TSX file |
| 12 | NavBar update + old file cleanup | 1 modified + 5 deleted files |
| 13 | End-to-end testing | Manual verification |
