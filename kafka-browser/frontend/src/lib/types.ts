export interface TopicInfo {
  name: string;
  partitions: number;
  internal: boolean;
}

export interface KafkaMessage {
  topic: string;
  partition: number;
  offset: number;
  timestamp: number;
  key: string | null;
  headers: Record<string, string>;
  schemaId: number | null;
  schemaType: string | null;
  schemaName: string | null;
  message: object | string | null;
  rawMessage: string | null;
  error: string | null;
}

export interface MessagePage {
  messages: KafkaMessage[];
  hasMore: boolean;
}

export interface MessageParams {
  partition?: number;
  offset?: number;
  limit?: number;
  useSchema?: boolean;
}

export interface TemplateSummary {
  id: number;
  name: string;
  collectionId: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface TemplateDetail extends TemplateSummary {
  jsonContent: string;
}

export interface TemplateFormData {
  name: string;
  collectionId: number | null;
  jsonContent: string;
}

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

export interface CollectionExport {
  name: string;
  topicName: string | null;
  schemaSubject: string | null;
  avscContent: string | null;
  templates: {
    name: string;
    jsonContent: string;
  }[];
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
