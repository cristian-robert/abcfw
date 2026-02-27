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
