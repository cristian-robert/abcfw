"use client";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Toggle } from "@/components/ui/toggle";
import { RefreshCw, FileJson, ArrowUpDown } from "lucide-react";
import type { SortOrder } from "./message-panel";

interface MessageToolbarProps {
  topic: string;
  partitionCount: number;
  partition: number;
  onPartitionChange: (partition: number) => void;
  limit: number;
  onLimitChange: (limit: number) => void;
  useSchema: boolean;
  onUseSchemaChange: (useSchema: boolean) => void;
  sortOrder: SortOrder;
  onSortOrderChange: (order: SortOrder) => void;
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
  sortOrder,
  onSortOrderChange,
  onRefresh,
  loading,
  messageCount,
}: MessageToolbarProps) {
  return (
    <div className="flex items-center gap-2 px-3 py-2 border-b border-white/[0.06] bg-[#0d0d0e] shrink-0 flex-wrap">
      <div className="flex items-center gap-2 min-w-0">
        <span className="text-xs font-mono text-teal-300 truncate max-w-[240px]">
          {topic}
        </span>
        {messageCount > 0 && (
          <Badge variant="secondary" className="text-[10px] h-4 px-1.5 font-mono shrink-0">
            {messageCount}
          </Badge>
        )}
      </div>

      <div className="flex-1 min-w-2" />

      <div className="flex items-center gap-1.5 flex-wrap">
        <Select
          value={String(partition)}
          onValueChange={(v) => onPartitionChange(Number(v))}
        >
          <SelectTrigger className="h-7 w-[110px] text-xs bg-white/[0.04] border-white/[0.06]">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="-1">All partitions</SelectItem>
            {Array.from({ length: partitionCount }).map((_, i) => (
              <SelectItem key={i} value={String(i)}>
                Partition {i}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select
          value={String(limit)}
          onValueChange={(v) => onLimitChange(Number(v))}
        >
          <SelectTrigger className="h-7 w-[72px] text-xs bg-white/[0.04] border-white/[0.06]">
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

        <Select
          value={sortOrder}
          onValueChange={(v) => onSortOrderChange(v as SortOrder)}
        >
          <SelectTrigger className="h-7 w-[110px] text-xs bg-white/[0.04] border-white/[0.06]">
            <ArrowUpDown className="h-3 w-3 mr-1 shrink-0" />
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="newest">Newest first</SelectItem>
            <SelectItem value="oldest">Oldest first</SelectItem>
          </SelectContent>
        </Select>

        <Toggle
          pressed={useSchema}
          onPressedChange={onUseSchemaChange}
          size="sm"
          className="h-7 gap-1 text-xs data-[state=on]:bg-teal-500/15 data-[state=on]:text-teal-300"
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
