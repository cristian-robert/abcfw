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
            <div className="text-xs text-destructive bg-destructive/10 rounded-md p-3 mb-2">
              {error}
            </div>
          )}

          {loading && messages.length === 0 && (
            <div className="space-y-2">
              {Array.from({ length: 5 }).map((_, i) => (
                <Skeleton key={i} className="h-12 w-full rounded-lg" />
              ))}
            </div>
          )}

          {!loading && messages.length === 0 && !error && (
            <div className="flex flex-col items-center justify-center py-16 text-muted-foreground/60">
              <p className="text-sm">No messages found</p>
              <p className="text-xs mt-1">
                This topic may be empty or the offset may be beyond the latest message
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
            <div className="flex justify-center pt-2 pb-4">
              <Button
                variant="outline"
                size="sm"
                onClick={loadMore}
                disabled={loading}
                className="text-xs"
              >
                {loading ? "Loading..." : "Load more"}
              </Button>
            </div>
          )}
        </div>
      </ScrollArea>
    </div>
  );
}
