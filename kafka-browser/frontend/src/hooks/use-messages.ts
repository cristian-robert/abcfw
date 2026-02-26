"use client";

import { useState, useCallback } from "react";
import { KafkaMessage, MessageParams } from "@/lib/types";
import { fetchMessages } from "@/lib/api";

export function useMessages() {
  const [messages, setMessages] = useState<KafkaMessage[]>([]);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(
    async (topic: string, params: MessageParams = {}, append = false) => {
      setLoading(true);
      setError(null);
      try {
        const data = await fetchMessages(topic, params);
        setMessages((prev) =>
          append ? [...prev, ...data.messages] : data.messages
        );
        setHasMore(data.hasMore);
      } catch (e) {
        setError(e instanceof Error ? e.message : "Failed to load messages");
      } finally {
        setLoading(false);
      }
    },
    []
  );

  const clear = useCallback(() => {
    setMessages([]);
    setHasMore(false);
    setError(null);
  }, []);

  return { messages, hasMore, loading, error, load, clear };
}
