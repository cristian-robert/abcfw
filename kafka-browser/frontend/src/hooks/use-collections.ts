"use client";

import { useState, useEffect, useCallback } from "react";
import {
  CollectionSummary,
  CollectionDetail,
  CollectionFormData,
  CollectionExport,
  BulkProduceResponse,
} from "@/lib/types";
import {
  fetchCollections,
  fetchCollection,
  createCollection,
  updateCollection,
  deleteCollection,
  runCollection,
  exportCollection,
  importCollection,
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

  const doExport = useCallback(async (id: number): Promise<void> => {
    try {
      const data = await exportCollection(id);
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${data.name}.collection.json`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to export collection");
    }
  }, []);

  const doImport = useCallback(async (data: CollectionExport): Promise<CollectionDetail | null> => {
    try {
      const result = await importCollection(data);
      await refresh();
      return result;
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to import collection");
      return null;
    }
  }, [refresh]);

  return { collections, loading, error, refresh, get, create, update, remove, run, doExport, doImport };
}
