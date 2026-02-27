"use client";

import { useState, useCallback } from "react";
import { ProduceResponse, BulkProduceResponse } from "@/lib/types";
import { produceMessage, produceBulk } from "@/lib/api";

export function useProducer() {
  const [sending, setSending] = useState(false);
  const [lastResult, setLastResult] = useState<ProduceResponse | null>(null);
  const [bulkResult, setBulkResult] = useState<BulkProduceResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const send = useCallback(async (topic: string, jsonContent: string, schemaSubject?: string) => {
    try {
      setSending(true);
      setError(null);
      const result = await produceMessage(topic, jsonContent, schemaSubject);
      setLastResult(result);
      if (!result.success) {
        setError(result.error);
      }
      return result;
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      setError(msg);
      return null;
    } finally {
      setSending(false);
    }
  }, []);

  const sendBulk = useCallback(async (topic: string, messages: string[], schemaSubject?: string) => {
    try {
      setSending(true);
      setError(null);
      setBulkResult(null);
      const result = await produceBulk(topic, messages, schemaSubject);
      setBulkResult(result);
      if (result.failed > 0) {
        setError(`${result.failed} of ${result.total} messages failed`);
      }
      return result;
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      setError(msg);
      return null;
    } finally {
      setSending(false);
    }
  }, []);

  const clearResults = useCallback(() => {
    setLastResult(null);
    setBulkResult(null);
    setError(null);
  }, []);

  return { sending, lastResult, bulkResult, error, send, sendBulk, clearResults };
}
