# Producer Page Refactor — Design Document

## Goal

Refactor the Templates page into a Producer page with collections (folders), auto-generated form fields, .avsc schema support, collection-level Kafka sending, confirmation dialogs, and a three-panel Postman-style layout.

## Requirements

1. **Rename**: Templates → Producer throughout UI and routes
2. **Collections**: Database-backed folders (one level deep) to organize templates
3. **Form-based editing**: Auto-generated form fields from JSON structure instead of tree/raw JSON editor
4. **Collection-level .avsc**: Upload Avro schema per collection for type-aware form generation
5. **Send collection**: Run all templates in a collection to a shared topic sequentially
6. **Confirmation dialog**: Required before any Kafka send (single or collection)
7. **Three-panel layout**: Collections sidebar | Template list | Form editor

## Data Model

### Collection Entity (new)

```
Collection
├── id: Long (auto-generated)
├── name: String (required) — e.g. "Payments", "Testing"
├── avscContent: CLOB (nullable) — raw .avsc schema content
├── schemaSubject: String (nullable) — Schema Registry subject
├── topicName: String (nullable) — target Kafka topic
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime
```

### Template Entity (modified)

```
Template
├── id: Long (unchanged)
├── name: String (unchanged)
├── jsonContent: CLOB (unchanged)
├── collectionId: Long (FK → Collection, nullable)
├── createdAt: LocalDateTime (unchanged)
└── updatedAt: LocalDateTime (unchanged)
```

**Removed from Template**: `topicName`, `schemaSubject` — these now live at the collection level.

### API Endpoints

**Collections:**
- `GET /api/collections` — list all collections
- `POST /api/collections` — create collection
- `GET /api/collections/{id}` — get collection with templates
- `PUT /api/collections/{id}` — update collection (name, topic, schema, avsc)
- `DELETE /api/collections/{id}` — delete collection (and its templates)
- `POST /api/collections/{id}/run` — send all templates in collection to Kafka

**Templates** (existing, modified):
- `GET /api/templates?collectionId={id}` — list templates in collection
- `POST /api/templates` — create (body includes collectionId)
- `GET /api/templates/{id}` — get template
- `PUT /api/templates/{id}` — update template
- `DELETE /api/templates/{id}` — delete template

**Producer** (existing, unchanged):
- `POST /api/produce` — send single message
- `GET /api/schemas/subjects` — list schema subjects

## Frontend Architecture

### Three-Panel Layout

```
┌────────────┬─────────────────┬──────────────────────────────────────┐
│ Collections│ Templates       │ Form Editor                          │
│ (~160px)   │ (~220px)        │ (flex-1)                             │
│            │                 │                                      │
│ ▶ Payments │ create-tx  ●    │ ┌─ Template: create-tx ────────────┐ │
│ ▼ Testing  │ update-tx       │ │                                  │ │
│ ▶ SEPA RT  │ refund-tx       │ │ ── header ─────────────────      │ │
│            │                 │ │ version    [ 1.0          ]      │ │
│            │ [+ New]         │ │ eventType  [ TX_CREATED   ]      │ │
│ [+ New]    │ ─────────────── │ │                                  │ │
│ [Upload    │ Topic: payments │ │ ── body ──────────────────       │ │
│  .avsc]    │ Schema: tx-v1   │ │ amount     [ 100.00       ]      │ │
│            │ [▶ Run All]     │ │ currency   [ EUR          ]      │ │
│            │                 │ │ [+ Add Field]                    │ │
│            │                 │ │                                  │ │
│            │                 │ │ [Save]  [Send to Kafka ▶]        │ │
│            │                 │ └──────────────────────────────────┘ │
└────────────┴─────────────────┴──────────────────────────────────────┘
```

### Panel 1 — Collections Sidebar

- Vertical list of collection names
- Click to select → populates Panel 2
- Selected collection highlighted with accent color
- Expand/collapse (visual only — one level deep)
- `[+ New Collection]` button at bottom
- `[Upload .avsc]` button (attaches to selected collection)
- Context actions: rename, delete (on hover or right-click)

### Panel 2 — Template List

- Lists templates in selected collection
- Click to select → populates Panel 3
- Selected template highlighted
- `[+ New Template]` button
  - If collection has `.avsc`: creates template with schema-derived empty form
  - If no `.avsc`: creates empty template
- Collection metadata section (bottom):
  - Topic name (editable inline)
  - Schema subject (editable inline, with autocomplete from Schema Registry)
- `[▶ Run All]` button — runs the collection

### Panel 3 — Form Editor

- **Template name** field at top
- **Auto-generated form fields** from JSON:
  - String → text input with label
  - Number → number input
  - Boolean → toggle/checkbox
  - Nested object → collapsible section with indent
  - Array → list of sub-forms with add/remove
  - Null → text input (treat as string)
- If `.avsc` available on collection:
  - Fields generated from Avro schema
  - Type-appropriate inputs (enum → dropdown, etc.)
  - Required fields marked
- `[+ Add Field]` button at each level
- Trash icon on hover to remove fields
- `[+ Add Section]` for nested objects
- Toggle: **Form** | **Raw JSON** (bidirectional sync)
- Action buttons: `[Save]` `[Send to Kafka ▶]`

### Confirmation Dialog

Before any Kafka send (single or collection):

```
┌─────────────────────────────────────┐
│ Confirm Send                        │
│                                     │
│ Send to topic: payments             │
│ Schema: tx-created-v1               │
│ Messages: 1 (or "5 templates")      │
│                                     │
│         [Cancel]  [Confirm Send]    │
└─────────────────────────────────────┘
```

## Components (Frontend)

### New Components
- `CollectionSidebar` — Panel 1: collection list + actions
- `TemplateListPanel` — Panel 2: template list + collection metadata + run
- `FormEditor` — Panel 3: auto-generated form from JSON/schema
- `FormField` — individual form field (text, number, boolean, section)
- `ConfirmSendDialog` — confirmation before Kafka send
- `AvscUploader` — .avsc file upload and parsing

### Modified Components
- `NavBar` — rename "Templates" to "Producer"
- `page.tsx` — completely rewritten for three-panel layout

### Removed Components
- `editable-json-tree.tsx` — replaced by FormEditor
- `template-list.tsx` — replaced by CollectionSidebar + TemplateListPanel
- `template-editor.tsx` — replaced by FormEditor
- `bulk-send-dialog.tsx` — replaced by collection run + ConfirmSendDialog

### Modified Hooks
- `useTemplates` → `useProducerTemplates` (adds collectionId filtering)
- `useProducer` — unchanged
- `useSchemas` — unchanged
- New: `useCollections` — CRUD for collections

## UX Flows

### Upload JSON Template
1. Select collection in Panel 1
2. Click `[+ New Template]` or drag-drop JSON onto Panel 2
3. Form generated from JSON structure (or schema if .avsc exists)
4. Edit field values in form
5. Click `[Save]` → persisted

### Upload .avsc Schema
1. Select collection in Panel 1
2. Click `[Upload .avsc]` → file picker
3. Schema stored on collection entity
4. New templates in collection get schema-aware forms
5. Existing templates keep their JSON but form rendering uses schema types

### Send Single Template
1. Select template → form shows in Panel 3
2. Click `[Send to Kafka ▶]`
3. Confirmation dialog: topic, schema, message count
4. Confirm → produce → success/error feedback

### Run Collection
1. Select collection in Panel 1
2. Click `[▶ Run All]` in Panel 2
3. Confirmation dialog: "Send 5 templates to topic X?"
4. Confirm → sequential send → per-template results

### AVSC-Aware Form Generation
When a collection has `.avsc`:
- Parse Avro schema to extract field names, types, defaults
- RECORD → nested form section
- STRING → text input
- INT/LONG → number input
- FLOAT/DOUBLE → number input (step=0.01)
- BOOLEAN → checkbox
- ENUM → dropdown with symbol options
- ARRAY → list with add/remove items
- MAP → key-value pairs with add/remove
- UNION (nullable) → field marked as optional

## Migration

- Existing templates get `collectionId = null` (uncategorized)
- Show "Uncategorized" virtual collection for orphan templates
- H2 DDL managed by Hibernate auto-update

## Tech Decisions

- **Schema Registry always available for producing** — .avsc is for form generation only
- **One-level-deep collections** — no nested folders
- **Topic + schema at collection level** — templates don't have their own topic/schema
- **Form/Raw JSON toggle** — keep raw JSON as fallback editor
- **Sequential collection run** — send templates one by one, show individual results
