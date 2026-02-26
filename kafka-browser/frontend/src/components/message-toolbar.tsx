"use client";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Toggle } from "@/components/ui/toggle";
import { RefreshCw, FileJson } from "lucide-react";

interface MessageToolbarProps {
  topic: string;
  partitionCount: number;
  partition: number;
  onPartitionChange: (partition: number) => void;
  limit: number;
  onLimitChange: (limit: number) => void;
  useSchema: boolean;
  onUseSchemaChange: (useSchema: boolean) => void;
  onRefresh: () => void;
  loading: boolean;
  messageCount: number;
}

export function MessageToolbar({
  topic,
  partitionCount,
  partition,
  onPartitionChange,
  limit,
  onLimitChange,
  useSchema,
  onUseSchemaChange,
  onRefresh,
  loading,
  messageCount,
}: MessageToolbarProps) {
  return (
    <div className="flex items-center gap-3 px-4 py-2.5 border-b border-border bg-[#0d0d0e]">
      <div className="flex items-center gap-1.5 min-w-0">
        <span className="text-xs font-mono text-teal-300 truncate max-w-[200px]">
          {topic}
        </span>
        {messageCount > 0 && (
          <span className="text-[10px] text-muted-foreground/60 shrink-0">
            ({messageCount} msg{messageCount !== 1 ? "s" : ""})
          </span>
        )}
      </div>

      <div className="flex-1" />

      <div className="flex items-center gap-2">
        <div className="flex items-center rounded-md border border-white/[0.08] bg-white/[0.04] overflow-hidden">
          <Select
            value={String(partition)}
            onValueChange={(v) => onPartitionChange(Number(v))}
          >
            <SelectTrigger className="h-7 w-[100px] text-xs border-0 bg-transparent shadow-none focus:ring-0 focus:ring-offset-0 rounded-none">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="-1">All parts.</SelectItem>
              {Array.from({ length: partitionCount }).map((_, i) => (
                <SelectItem key={i} value={String(i)}>
                  Part. {i}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <div className="w-px h-4 bg-white/[0.1] shrink-0" />
          <Select
            value={String(limit)}
            onValueChange={(v) => onLimitChange(Number(v))}
          >
            <SelectTrigger className="h-7 w-[70px] text-xs border-0 bg-transparent shadow-none focus:ring-0 focus:ring-offset-0 rounded-none">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {[10, 25, 50, 100].map((n) => (
                <SelectItem key={n} value={String(n)}>
                  {n}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <Toggle
          pressed={useSchema}
          onPressedChange={onUseSchemaChange}
          size="sm"
          className={cn(
            "h-7 gap-1.5 text-xs",
            "data-[state=on]:bg-emerald-500/15 data-[state=on]:text-emerald-300",
            useSchema && "shadow-[0_0_10px_rgba(16,185,129,0.15)]"
          )}
          aria-label="Toggle schema deserialization"
        >
          <FileJson className="h-3.5 w-3.5" />
          Schema
        </Toggle>

        <Button
          variant="ghost"
          size="icon"
          className="h-7 w-7"
          onClick={onRefresh}
          disabled={loading}
        >
          <RefreshCw
            className={`h-3.5 w-3.5 ${loading ? "animate-spin" : ""}`}
          />
        </Button>
      </div>
    </div>
  );
}
