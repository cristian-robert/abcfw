"use client";

import { useState, useEffect, useCallback } from "react";
import { TemplateSummary, TemplateDetail, TemplateFormData } from "@/lib/types";
import {
  fetchTemplates,
  fetchTemplate,
  createTemplate,
  updateTemplate,
  deleteTemplate,
  duplicateTemplate,
} from "@/lib/api";

export function useTemplates(collectionId?: number | null) {
  const [templates, setTemplates] = useState<TemplateSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchTemplates(collectionId ?? undefined);
      setTemplates(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch templates");
    } finally {
      setLoading(false);
    }
  }, [collectionId]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const get = useCallback(async (id: number): Promise<TemplateDetail | null> => {
    try {
      return await fetchTemplate(id);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch template");
      return null;
    }
  }, []);

  const create = useCallback(async (data: TemplateFormData): Promise<TemplateDetail | null> => {
    try {
      const result = await createTemplate(data);
      await refresh();
      return result;
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create template");
      return null;
    }
  }, [refresh]);

  const update = useCallback(async (id: number, data: TemplateFormData): Promise<TemplateDetail | null> => {
    try {
      const result = await updateTemplate(id, data);
      await refresh();
      return result;
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update template");
      return null;
    }
  }, [refresh]);

  const remove = useCallback(async (id: number): Promise<boolean> => {
    try {
      await deleteTemplate(id);
      await refresh();
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete template");
      return false;
    }
  }, [refresh]);

  const duplicate = useCallback(async (id: number): Promise<TemplateDetail | null> => {
    try {
      const result = await duplicateTemplate(id);
      await refresh();
      return result;
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to duplicate template");
      return null;
    }
  }, [refresh]);

  return { templates, loading, error, refresh, get, create, update, remove, duplicate };
}
