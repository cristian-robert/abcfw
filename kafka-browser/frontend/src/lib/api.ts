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
