import { TopicInfo, MessagePage, MessageParams } from "./types";

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
