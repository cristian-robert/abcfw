"use client";

import { useState, useEffect } from "react";
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
  const [isConnected, setIsConnected] = useState<boolean | null>(null);

  useEffect(() => {
    const checkHealth = async () => {
      try {
        const res = await fetch("/api/health");
        setIsConnected(res.ok);
      } catch {
        setIsConnected(false);
      }
    };

    checkHealth();
    const interval = setInterval(checkHealth, 30000);
    return () => clearInterval(interval);
  }, []);

  return (
    <aside className="w-[280px] shrink-0 border-r border-border flex flex-col bg-[#0d0d0e]">
      <div className="p-4 border-b border-border">
        <div className="flex items-center gap-2 mb-3">
          <Database className="h-4 w-4 text-teal-400" />
          <h1 className="text-sm font-semibold tracking-tight text-gradient-teal">
            Kafka Browser
          </h1>
          <div className="flex-1" />
          <div
            className={cn(
              "h-2 w-2 rounded-full shrink-0 transition-colors",
              isConnected === null
                ? "bg-zinc-500"
                : isConnected
                  ? "bg-emerald-400 animate-pulse-dot"
                  : "bg-red-400"
            )}
            title={
              isConnected === null
                ? "Checking connection..."
                : isConnected
                  ? "Connected"
                  : "Disconnected"
            }
          />
        </div>
        <div className="relative">
          <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-muted-foreground" />
          <Input
            placeholder="Filter topics..."
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            className="pl-8 h-8 text-xs bg-white/[0.04] border-white/[0.08] focus-visible:ring-teal-500/30"
          />
        </div>
        {search && (
          <p className="text-[10px] text-muted-foreground/50 mt-1.5 px-0.5">
            {topics.length} of {totalCount} topics
          </p>
        )}
      </div>

      <ScrollArea className="flex-1">
        <div className="p-2">
          {loading && (
            <div className="space-y-1.5 p-1">
              {Array.from({ length: 8 }).map((_, i) => (
                <Skeleton key={i} className="h-8 w-full rounded-md" />
              ))}
            </div>
          )}

          {error && (
            <div className="p-3 text-xs text-destructive">{error}</div>
          )}

          {!loading &&
            !error &&
            topics.map((topic) => (
              <button
                key={topic.name}
                onClick={() => onSelectTopic(topic.name)}
                className={cn(
                  "w-full flex items-center justify-between gap-2 px-3 py-2 rounded-md text-left text-xs transition-all duration-200 border-l-2",
                  "hover:bg-white/[0.07]",
                  selectedTopic === topic.name
                    ? "bg-teal-500/10 text-teal-300 border-teal-400 shadow-[inset_2px_0_8px_rgba(45,212,191,0.06)]"
                    : "text-muted-foreground border-transparent"
                )}
                style={{ animationDelay: `${topics.indexOf(topic) * 20}ms` }}
              >
                <span className="truncate font-mono">{topic.name}</span>
                <Badge
                  variant="secondary"
                  className="shrink-0 text-[10px] h-4 px-1.5 font-mono"
                >
                  {topic.partitions}
                </Badge>
              </button>
            ))}

          {!loading && !error && topics.length === 0 && (
            <div className="p-4 text-center text-xs text-muted-foreground">
              {search ? "No matching topics" : "No topics found"}
            </div>
          )}
        </div>
      </ScrollArea>

      <div className="p-3 border-t border-border text-[10px] text-muted-foreground/60">
        {topics.length} of {totalCount} topics
      </div>
    </aside>
  );
}
