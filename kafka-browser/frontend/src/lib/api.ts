import {
  TopicInfo,
  MessagePage,
  MessageParams,
  TemplateSummary,
  TemplateDetail,
  TemplateFormData,
  CollectionSummary,
  CollectionDetail,
  CollectionFormData,
  ProduceResponse,
  BulkProduceResponse,
} from "./types";

export async function fetchTopics(
  includeInternal = false
): Promise<TopicInfo[]> {
  const res = await fetch(
    `/api/topics?includeInternal=${includeInternal}`
  );
  if (!res.ok) throw new Error(`Failed to fetch topics: ${res.status}`);
  return res.json();
}

export async function fetchMessages(
  topic: string,
  params: MessageParams = {}
): Promise<MessagePage> {
  const searchParams = new URLSearchParams();
  if (params.partition !== undefined && params.partition !== -1) {
    searchParams.set("partition", String(params.partition));
  }
  if (params.offset !== undefined && params.offset !== -1) {
    searchParams.set("offset", String(params.offset));
  }
  searchParams.set("limit", String(params.limit ?? 25));
  searchParams.set("useSchema", String(params.useSchema ?? true));

  const res = await fetch(
    `/api/topics/${encodeURIComponent(topic)}/messages?${searchParams}`
  );
  if (!res.ok) throw new Error(`Failed to fetch messages: ${res.status}`);
  return res.json();
}

// --- Templates ---

export async function fetchTemplates(collectionId?: number): Promise<TemplateSummary[]> {
  const url = collectionId
    ? `/api/templates?collectionId=${collectionId}`
    : "/api/templates";
  const res = await fetch(url);
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
