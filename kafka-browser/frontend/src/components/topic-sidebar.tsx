"use client";

import { useState, useEffect } from "react";
import { TopicInfo } from "@/lib/types";
import { cn } from "@/lib/utils";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Skeleton } from "@/components/ui/skeleton";
import { Search, Database, X, Wifi, WifiOff } from "lucide-react";

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

  const statusLabel =
    isConnected === null
      ? "Checking connection..."
      : isConnected
        ? "Connected to Kafka"
        : "Disconnected";

  return (
    <aside className="w-[280px] shrink-0 border-r border-border flex flex-col bg-[#0d0d0e]">
      {/* Header */}
      <div className="p-4 border-b border-border">
        <div className="flex items-center gap-2 mb-3">
          <Database className="h-4 w-4 text-teal-400" aria-hidden="true" />
          <h1 className="text-sm font-semibold tracking-tight text-gradient-teal">
            Kafka Browser
          </h1>
          <div className="flex-1" />
          <div
            role="status"
            aria-live="polite"
            aria-label={statusLabel}
            className={cn(
              "h-2 w-2 rounded-full shrink-0 transition-colors duration-300",
              isConnected === null
                ? "bg-zinc-500"
                : isConnected
                  ? "bg-emerald-400 animate-pulse-dot"
                  : "bg-red-400"
            )}
            title={statusLabel}
          />
        </div>

        {/* Search input with clear button */}
        <div className="relative">
          <Search
            className="absolute left-2.5 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-muted-foreground/50"
            aria-hidden="true"
          />
          <Input
            placeholder="Filter topics..."
            aria-label="Filter topics"
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            className="pl-8 pr-7 h-8 text-xs bg-white/[0.04] border-white/[0.08] focus-visible:ring-teal-500/30 placeholder:text-muted-foreground/30"
          />
          {search && (
            <button
              type="button"
              onClick={() => onSearchChange("")}
              className="absolute right-2 top-1/2 -translate-y-1/2 p-0.5 rounded-sm text-muted-foreground/40 hover:text-muted-foreground transition-colors duration-150"
              aria-label="Clear search"
            >
              <X className="h-3 w-3" aria-hidden="true" />
            </button>
          )}
        </div>

        {/* Filter count */}
        {search && (
          <p
            className="text-[10px] text-muted-foreground/40 mt-1.5 px-0.5"
            aria-live="polite"
          >
            {topics.length} of {totalCount} topics
          </p>
        )}
      </div>

      {/* Topic list */}
      <ScrollArea className="flex-1">
        <nav className="p-2" aria-label="Topic list">
          {loading && (
            <div className="space-y-1 p-1">
              {Array.from({ length: 10 }).map((_, i) => (
                <div key={i} className="flex items-center gap-2 px-3 py-2">
                  <Skeleton className="h-3 flex-1 rounded" />
                  <Skeleton className="h-4 w-6 rounded" />
                </div>
              ))}
            </div>
          )}

          {error && (
            <div className="p-3 text-xs text-destructive" role="alert">
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
                  aria-current={isSelected ? "true" : undefined}
                  className={cn(
                    "w-full flex items-center justify-between gap-2 px-3 py-2 rounded-md text-left text-xs",
                    "transition-colors duration-150 border-l-2 cursor-pointer",
                    "hover:bg-white/[0.06]",
                    "focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-teal-500/50",
                    isSelected
                      ? "bg-teal-500/10 text-teal-300 border-teal-400"
                      : "text-muted-foreground/70 border-transparent hover:text-muted-foreground"
                  )}
                >
                  <span className="truncate font-mono">{topic.name}</span>
                  <Badge
                    variant="secondary"
                    className={cn(
                      "shrink-0 text-[10px] h-4 px-1.5 font-mono",
                      isSelected && "bg-teal-500/15 text-teal-400/70"
                    )}
                  >
                    {topic.partitions}
                  </Badge>
                </button>
              );
            })}

          {!loading && !error && topics.length === 0 && (
            <div className="p-4 text-center text-xs text-muted-foreground/40">
              {search ? "No matching topics" : "No topics found"}
            </div>
          )}
        </nav>
      </ScrollArea>

      {/* Footer */}
      <div className="p-3 border-t border-border flex items-center gap-2">
        <div className="flex-1 text-[10px] text-muted-foreground/40 font-mono">
          {totalCount} topic{totalCount !== 1 ? "s" : ""}
        </div>
        <div
          className={cn(
            "flex items-center gap-1 text-[10px]",
            isConnected === null
              ? "text-muted-foreground/30"
              : isConnected
                ? "text-emerald-400/50"
                : "text-red-400/50"
          )}
        >
          {isConnected ? (
            <Wifi className="h-2.5 w-2.5" aria-hidden="true" />
          ) : (
            <WifiOff className="h-2.5 w-2.5" aria-hidden="true" />
          )}
          <span className="font-mono">
            {isConnected === null ? "..." : isConnected ? "live" : "offline"}
          </span>
        </div>
      </div>
    </aside>
  );
}
