"use client";

import { useState, useCallback, useEffect } from "react";
import { TemplateSummary, TemplateDetail, TemplateFormData } from "@/lib/types";
import {
  fetchTemplates,
  fetchTemplate,
  createTemplate,
  updateTemplate,
  deleteTemplate,
} from "@/lib/api";

export function useTemplates() {
  const [templates, setTemplates] = useState<TemplateSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchTemplates();
      setTemplates(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const get = useCallback(async (id: number): Promise<TemplateDetail | null> => {
    try {
      return await fetchTemplate(id);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      return null;
    }
  }, []);

  const create = useCallback(async (data: TemplateFormData): Promise<TemplateDetail | null> => {
    try {
      const result = await createTemplate(data);
      await refresh();
      return result;
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      return null;
    }
  }, [refresh]);

  const update = useCallback(async (id: number, data: TemplateFormData): Promise<TemplateDetail | null> => {
    try {
      const result = await updateTemplate(id, data);
      await refresh();
      return result;
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      return null;
    }
  }, [refresh]);

  const remove = useCallback(async (id: number): Promise<boolean> => {
    try {
      await deleteTemplate(id);
      await refresh();
      return true;
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      return false;
    }
  }, [refresh]);

  return { templates, loading, error, refresh, get, create, update, remove };
}
