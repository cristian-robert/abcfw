"use client";

import { useState, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import {
  Send,
  X,
  FileJson,
  CheckCircle,
  XCircle,
  Loader2,
} from "lucide-react";
import { BulkProduceResponse } from "@/lib/types";

interface BulkSendFile {
  name: string;
  content: string;
}

interface BulkSendDialogProps {
  files: BulkSendFile[];
  subjects: string[];
  onSend: (
    topic: string,
    messages: string[],
    schemaSubject?: string
  ) => Promise<BulkProduceResponse | null>;
  onClose: () => void;
}

export function BulkSendDialog({
  files,
  subjects,
  onSend,
  onClose,
}: BulkSendDialogProps) {
  const [topic, setTopic] = useState("");
  const [schemaSubject, setSchemaSubject] = useState("");
  const [sending, setSending] = useState(false);
  const [result, setResult] = useState<BulkProduceResponse | null>(null);

  const handleSend = useCallback(async () => {
    if (!topic.trim() || files.length === 0) return;
    setSending(true);
    const messages = files.map((f) => f.content);
    const res = await onSend(
      topic,
      messages,
      schemaSubject || undefined
    );
    setResult(res);
    setSending(false);
  }, [topic, schemaSubject, files, onSend]);

  return (
    <div className="flex flex-col h-full">
      <div className="p-3 border-b border-white/[0.06] flex items-center justify-between">
        <h3 className="text-sm font-semibold text-foreground">
          Bulk Send — {files.length} file{files.length !== 1 ? "s" : ""}
        </h3>
        <button onClick={onClose} className="text-muted-foreground hover:text-foreground">
          <X className="h-4 w-4" />
        </button>
      </div>

      <div className="p-3 border-b border-white/[0.06] space-y-2">
        <Input
          value={topic}
          onChange={(e) => setTopic(e.target.value)}
          placeholder="Target topic"
          className="h-8 text-sm font-mono"
        />
        <Input
          value={schemaSubject}
          onChange={(e) => setSchemaSubject(e.target.value)}
          placeholder="Schema subject (optional)"
          className="h-8 text-sm font-mono"
          list="bulk-schema-subjects"
        />
        <datalist id="bulk-schema-subjects">
          {subjects.map((s) => (
            <option key={s} value={s} />
          ))}
        </datalist>
      </div>

      <ScrollArea className="flex-1">
        <div className="p-3 space-y-1">
          {files.map((file, i) => (
            <div
              key={i}
              className="flex items-center gap-2 px-2 py-1.5 rounded-md bg-white/[0.02] text-sm"
            >
              <FileJson className="h-4 w-4 text-muted-foreground shrink-0" />
              <span className="flex-1 truncate font-mono text-xs">
                {file.name}.json
              </span>
              <Badge variant="outline" className="text-[10px]">
                {(file.content.length / 1024).toFixed(1)}KB
              </Badge>
              {result && result.results[i] && (
                result.results[i].success ? (
                  <CheckCircle className="h-4 w-4 text-emerald-400 shrink-0" />
                ) : (
                  <XCircle className="h-4 w-4 text-red-400 shrink-0" title={result.results[i].error || undefined} />
                )
              )}
            </div>
          ))}
        </div>
      </ScrollArea>

      <div className="p-3 border-t border-white/[0.06] flex items-center gap-2">
        {result ? (
          <>
            <span className="text-sm">
              <span className="text-emerald-400">{result.succeeded} sent</span>
              {result.failed > 0 && (
                <span className="text-red-400 ml-2">{result.failed} failed</span>
              )}
            </span>
            <Button variant="outline" size="sm" onClick={onClose} className="ml-auto">
              Close
            </Button>
          </>
        ) : (
          <Button
            size="sm"
            onClick={handleSend}
            disabled={sending || !topic.trim()}
            className="bg-teal-600 hover:bg-teal-700 text-white ml-auto"
          >
            {sending ? (
              <Loader2 className="h-4 w-4 mr-1 animate-spin" />
            ) : (
              <Send className="h-4 w-4 mr-1" />
            )}
            {sending ? "Sending..." : `Send ${files.length} messages`}
          </Button>
        )}
      </div>
    </div>
  );
}
