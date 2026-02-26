"use client";

import { useEffect, useCallback, useState } from "react";
import { TopicInfo, KafkaMessage } from "@/lib/types";
import { useMessages } from "@/hooks/use-messages";
import { MessageToolbar } from "./message-toolbar";
import { MessageCard } from "./message-card";
import { EmptyState } from "./empty-state";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { ScrollArea } from "@/components/ui/scroll-area";
import { ChevronDown, AlertCircle, Inbox, RefreshCw } from "lucide-react";

interface MessagePanelProps {
  selectedTopic: string | null;
  topicInfo: TopicInfo | undefined;
}

export function MessagePanel({ selectedTopic, topicInfo }: MessagePanelProps) {
  const { messages, hasMore, loading, error, load, clear } = useMessages();
  const [partition, setPartition] = useState(-1);
  const [limit, setLimit] = useState(25);
  const [useSchema, setUseSchema] = useState(true);

  const refresh = useCallback(() => {
    if (!selectedTopic) return;
    load(selectedTopic, { partition, limit, useSchema });
  }, [selectedTopic, partition, limit, useSchema, load]);

  useEffect(() => {
    if (selectedTopic) {
      setPartition(-1);
      load(selectedTopic, { partition: -1, limit, useSchema });
    } else {
      clear();
    }
  }, [selectedTopic]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (selectedTopic) {
      load(selectedTopic, { partition, limit, useSchema });
    }
  }, [partition, limit, useSchema]); // eslint-disable-line react-hooks/exhaustive-deps

  const loadMore = useCallback(() => {
    if (!selectedTopic || !messages.length) return;
    const lastOffset = messages[messages.length - 1].offset;
    load(
      selectedTopic,
      { partition, offset: lastOffset + 1, limit, useSchema },
      true
    );
  }, [selectedTopic, messages, partition, limit, useSchema, load]);

  if (!selectedTopic) {
    return <EmptyState />;
  }

  return (
    <div className="flex-1 flex flex-col min-w-0">
      <MessageToolbar
        topic={selectedTopic}
        partitionCount={topicInfo?.partitions ?? 0}
        partition={partition}
        onPartitionChange={setPartition}
        limit={limit}
        onLimitChange={setLimit}
        useSchema={useSchema}
        onUseSchemaChange={setUseSchema}
        onRefresh={refresh}
        loading={loading}
        messageCount={messages.length}
      />

      <ScrollArea className="flex-1">
        <div className="p-4 space-y-2">
          {error && (
            <div className="flex items-center gap-2.5 text-xs text-destructive bg-destructive/10 rounded-lg p-3 ring-1 ring-destructive/20 mb-2">
              <AlertCircle
                className="h-3.5 w-3.5 shrink-0"
                aria-hidden="true"
              />
              <span className="flex-1">{error}</span>
              <Button
                variant="ghost"
                size="sm"
                onClick={refresh}
                className="h-6 text-[10px] text-destructive hover:text-destructive px-2"
              >
                <RefreshCw className="h-3 w-3 mr-1" aria-hidden="true" />
                Retry
              </Button>
            </div>
          )}

          {loading && messages.length === 0 && (
            <div className="space-y-2">
              {Array.from({ length: 5 }).map((_, i) => (
                <div
                  key={i}
                  className="rounded-lg ring-1 ring-white/[0.06] bg-[#111113] p-3"
                >
                  <div className="flex items-center gap-3">
                    <Skeleton className="h-3 w-3 rounded" />
                    <Skeleton className="h-4 w-12 rounded" />
                    <Skeleton className="h-4 w-16 rounded" />
                    <Skeleton className="h-3 w-28 rounded" />
                    <div className="flex-1" />
                    <Skeleton className="h-3 w-16 rounded" />
                  </div>
                </div>
              ))}
            </div>
          )}

          {!loading && messages.length === 0 && !error && (
            <div className="flex flex-col items-center justify-center py-20 text-muted-foreground/30">
              <Inbox
                className="h-10 w-10 mb-3 stroke-[1]"
                aria-hidden="true"
              />
              <p className="text-sm font-medium text-muted-foreground/50">
                No messages found
              </p>
              <p className="text-xs text-muted-foreground/30 mt-1">
                This topic may be empty or the offset may be beyond the latest
                message
              </p>
            </div>
          )}

          {messages.map((msg: KafkaMessage, i: number) => (
            <MessageCard
              key={`${msg.partition}-${msg.offset}`}
              message={msg}
              index={i}
            />
          ))}

          {hasMore && (
            <div className="flex justify-center pt-3 pb-4">
              <Button
                variant="outline"
                size="sm"
                onClick={loadMore}
                disabled={loading}
                className="text-xs gap-1.5 h-8 px-4 ring-1 ring-white/[0.06] border-0 bg-white/[0.03] hover:bg-white/[0.06]"
              >
                {loading ? (
                  <>
                    <RefreshCw
                      className="h-3 w-3 animate-spin"
                      aria-hidden="true"
                    />
                    Loading...
                  </>
                ) : (
                  <>
                    <ChevronDown className="h-3 w-3" aria-hidden="true" />
                    Load more
                  </>
                )}
              </Button>
            </div>
          )}
        </div>
      </ScrollArea>
    </div>
  );
}
