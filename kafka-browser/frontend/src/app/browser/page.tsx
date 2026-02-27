"use client";

import { useState, useMemo } from "react";
import { useTopics } from "@/hooks/use-topics";
import { TopicSidebar } from "@/components/topic-sidebar";
import { MessagePanel } from "@/components/message-panel";

export default function Home() {
  const { topics, allTopics, loading, error, search, setSearch, refresh } =
    useTopics();
  const [selectedTopic, setSelectedTopic] = useState<string | null>(null);

  const topicInfo = useMemo(
    () => allTopics.find((t) => t.name === selectedTopic),
    [allTopics, selectedTopic]
  );

  return (
    <div className="flex h-full overflow-hidden">
      <TopicSidebar
        topics={topics}
        loading={loading}
        error={error}
        search={search}
        onSearchChange={setSearch}
        selectedTopic={selectedTopic}
        onSelectTopic={setSelectedTopic}
        totalCount={allTopics.length}
      />
      <MessagePanel selectedTopic={selectedTopic} topicInfo={topicInfo} />
    </div>
  );
}
