"use client";

import { useState, useCallback, useEffect } from "react";
import { fetchSettings, updateSetting } from "@/lib/api";

export function useSettings() {
  const [settings, setSettings] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      setLoading(true);
      const data = await fetchSettings();
      setSettings(data);
    } catch {
      setSettings({});
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const set = useCallback(async (key: string, value: string) => {
    try {
      await updateSetting(key, value);
      setSettings((prev) => ({ ...prev, [key]: value }));
    } catch {
      // silent
    }
  }, []);

  return { settings, loading, refresh, set };
}
