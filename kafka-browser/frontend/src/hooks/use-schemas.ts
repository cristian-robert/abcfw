"use client";

import { useState, useCallback, useEffect } from "react";
import { fetchSchemaSubjects } from "@/lib/api";

export function useSchemas() {
  const [subjects, setSubjects] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      setLoading(true);
      const data = await fetchSchemaSubjects();
      setSubjects(data);
    } catch {
      setSubjects([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  return { subjects, loading, refresh };
}
