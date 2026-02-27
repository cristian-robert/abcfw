"use client";

import { useRef } from "react";
import { cn } from "@/lib/utils";
import { TemplateSummary, CollectionDetail } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Skeleton } from "@/components/ui/skeleton";
import { Plus, Play, Trash2, FileJson } from "lucide-react";

interface TemplateListPanelProps {
  templates: TemplateSummary[];
  loading: boolean;
  selectedId: number | null;
  collection: CollectionDetail | null;
  subjects: string[];
  onSelect: (id: number) => void;
  onUpload: (name: string, content: string) => void;
  onDelete: (id: number) => void;
  onRunAll: () => void;
  onTopicChange: (topic: string) => void;
  onSchemaChange: (schema: string) => void;
}

export function TemplateListPanel({
  templates,
  loading,
  selectedId,
  collection,
  subjects,
  onSelect,
  onUpload,
  onDelete,
  onRunAll,
  onTopicChange,
  onSchemaChange,
}: TemplateListPanelProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;
    for (const file of Array.from(files)) {
      const content = await file.text();
      const name = file.name.replace(/\.json$/i, "");
      onUpload(name, content);
    }
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  return (
    <div className="flex flex-col h-full border-r border-white/[0.06] w-[240px] shrink-0">
      <div className="flex items-center justify-between px-3 h-10 border-b border-white/[0.06] shrink-0">
        <span className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
          Templates
        </span>
        <Button
          variant="ghost"
          size="icon"
          className="h-6 w-6"
          onClick={() => fileInputRef.current?.click()}
        >
          <Plus className="h-3.5 w-3.5" />
        </Button>
        <input
          ref={fileInputRef}
          type="file"
          accept=".json"
          multiple
          className="hidden"
          onChange={handleFileUpload}
        />
      </div>

      <ScrollArea className="flex-1">
        <div className="p-1.5">
          {loading && (
            <div className="space-y-1">
              {Array.from({ length: 5 }).map((_, i) => (
                <Skeleton key={i} className="h-8 w-full rounded-md" />
              ))}
            </div>
          )}

          {!loading && templates.length === 0 && (
            <div className="text-xs text-muted-foreground/60 text-center py-8 px-2">
              No templates yet. Click + to upload JSON files.
            </div>
          )}

          {!loading &&
            templates.map((t) => {
              const isSelected = t.id === selectedId;
              return (
                <div
                  key={t.id}
                  className={cn(
                    "group flex items-center gap-1.5 px-2 py-1.5 rounded-md cursor-pointer text-sm transition-colors",
                    isSelected
                      ? "bg-white/[0.08] text-teal-400"
                      : "text-muted-foreground hover:text-foreground hover:bg-white/[0.04]"
                  )}
                  onClick={() => onSelect(t.id)}
                >
                  <FileJson className="h-3.5 w-3.5 shrink-0" />
                  <span className="truncate flex-1">{t.name}</span>
                  <button
                    className="hidden group-hover:block p-0.5 rounded hover:bg-red-500/20 text-red-400"
                    onClick={(e) => {
                      e.stopPropagation();
                      onDelete(t.id);
                    }}
                  >
                    <Trash2 className="h-3 w-3" />
                  </button>
                </div>
              );
            })}
        </div>
      </ScrollArea>

      {collection && (
        <div className="border-t border-white/[0.06] p-3 space-y-2 shrink-0">
          <div>
            <label className="text-[10px] uppercase tracking-wider text-muted-foreground/60">
              Topic
            </label>
            <Input
              value={collection.topicName || ""}
              onChange={(e) => onTopicChange(e.target.value)}
              placeholder="topic-name"
              className="h-7 text-xs mt-0.5"
            />
          </div>
          <div>
            <label className="text-[10px] uppercase tracking-wider text-muted-foreground/60">
              Schema Subject
            </label>
            <Input
              value={collection.schemaSubject || ""}
              onChange={(e) => onSchemaChange(e.target.value)}
              placeholder="schema-subject"
              list="schema-subjects"
              className="h-7 text-xs mt-0.5"
            />
            <datalist id="schema-subjects">
              {subjects.map((s) => (
                <option key={s} value={s} />
              ))}
            </datalist>
          </div>
          <Button
            size="sm"
            className="w-full h-7 text-xs"
            onClick={onRunAll}
            disabled={!collection.topicName || templates.length === 0}
          >
            <Play className="h-3 w-3 mr-1.5" />
            Run All ({templates.length})
          </Button>
        </div>
      )}
    </div>
  );
}
