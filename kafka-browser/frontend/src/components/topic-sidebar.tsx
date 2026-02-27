"use client";

import { TopicInfo } from "@/lib/types";
import { cn } from "@/lib/utils";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Skeleton } from "@/components/ui/skeleton";
import { Search, Database } from "lucide-react";

interface TopicSidebarProps {
  topics: TopicInfo[];
  loading: boolean;
  error: string | null;
  search: string;
  onSearchChange: (value: string) => void;
  selectedTopic: string | null;
  onSelectTopic: (topic: string) => void;
  totalCount: number;
}

export function TopicSidebar({
  topics,
  loading,
  error,
  search,
  onSearchChange,
  selectedTopic,
  onSelectTopic,
  totalCount,
}: TopicSidebarProps) {
  return (
    <aside className="w-[280px] shrink-0 border-r border-white/[0.06] flex flex-col bg-[#0d0d0e]">
      <div className="px-3 py-3 border-b border-white/[0.06] shrink-0">
        <div className="flex items-center gap-2 mb-2.5">
          <Database className="h-4 w-4 text-teal-400" />
          <h1 className="text-sm font-semibold tracking-tight">Topics</h1>
          <Badge
            variant="secondary"
            className="ml-auto text-[10px] h-4 px-1.5 font-mono"
          >
            {topics.length}/{totalCount}
          </Badge>
        </div>
        <div className="relative">
          <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-muted-foreground/50" />
          <Input
            placeholder="Filter topics..."
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            className="pl-8 h-7 text-xs bg-white/[0.04] border-white/[0.06] focus-visible:ring-teal-500/30"
          />
        </div>
      </div>

      <ScrollArea className="flex-1 min-h-0">
        <div className="p-1.5">
          {loading && (
            <div className="flex flex-col gap-1 p-1">
              {Array.from({ length: 10 }).map((_, i) => (
                <Skeleton key={i} className="h-7 w-full rounded-md" />
              ))}
            </div>
          )}

          {error && (
            <div className="p-3 text-xs text-destructive bg-destructive/10 rounded-md m-1">
              {error}
            </div>
          )}

          {!loading &&
            !error &&
            topics.map((topic) => {
              const isSelected = selectedTopic === topic.name;
              return (
                <button
                  key={topic.name}
                  onClick={() => onSelectTopic(topic.name)}
                  className={cn(
                    "w-full flex items-center gap-2 px-2.5 py-1.5 rounded-md text-left text-xs transition-colors cursor-pointer",
                    isSelected
                      ? "bg-teal-500/10 text-teal-300"
                      : "text-muted-foreground hover:text-foreground hover:bg-white/[0.04]"
                  )}
                >
                  <span className="truncate flex-1 font-mono text-[11px]">
                    {topic.name}
                  </span>
                  <Badge
                    variant="secondary"
                    className={cn(
                      "shrink-0 text-[9px] h-4 px-1 font-mono",
                      isSelected && "bg-teal-500/15 text-teal-400"
                    )}
                  >
                    {topic.partitions}p
                  </Badge>
                </button>
              );
            })}

          {!loading && !error && topics.length === 0 && (
            <div className="p-6 text-center text-xs text-muted-foreground/50">
              {search ? "No matching topics" : "No topics found"}
            </div>
          )}
        </div>
      </ScrollArea>
    </aside>
  );
}
