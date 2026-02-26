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
