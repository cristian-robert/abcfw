"use client";

import { useState, useEffect, useCallback, useMemo } from "react";
import { TopicInfo } from "@/lib/types";
import { fetchTopics } from "@/lib/api";

export function useTopics() {
  const [topics, setTopics] = useState<TopicInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchTopics(false);
      setTopics(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to load topics");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const filtered = useMemo(() => {
    if (!search.trim()) return topics;
    const q = search.toLowerCase();
    return topics.filter((t) => t.name.toLowerCase().includes(q));
  }, [topics, search]);

  return { topics: filtered, allTopics: topics, loading, error, search, setSearch, refresh: load };
}
