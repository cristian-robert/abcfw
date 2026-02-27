"use client";

import { TemplateSummary } from "@/lib/types";
import { cn } from "@/lib/utils";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Upload, FileJson, Trash2, FolderOpen } from "lucide-react";
import { useCallback, useRef } from "react";

interface TemplateListProps {
  templates: TemplateSummary[];
  loading: boolean;
  selectedId: number | null;
  onSelect: (id: number) => void;
  onUpload: (name: string, content: string) => void;
  onBulkUpload: (files: { name: string; content: string }[]) => void;
  onDelete: (id: number) => void;
}

export function TemplateList({
  templates,
  loading,
  selectedId,
  onSelect,
  onUpload,
  onBulkUpload,
  onDelete,
}: TemplateListProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const bulkInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>) => {
      const files = e.target.files;
      if (!files?.length) return;
      const file = files[0];
      const content = await file.text();
      const name = file.name.replace(/\.json$/i, "");
      onUpload(name, content);
      e.target.value = "";
    },
    [onUpload]
  );

  const handleBulkChange = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>) => {
      const files = e.target.files;
      if (!files?.length) return;
      const results: { name: string; content: string }[] = [];
      for (const file of Array.from(files)) {
        if (!file.name.endsWith(".json")) continue;
        const content = await file.text();
        results.push({ name: file.name.replace(/\.json$/i, ""), content });
      }
      if (results.length > 0) onBulkUpload(results);
      e.target.value = "";
    },
    [onBulkUpload]
  );

  const handleDrop = useCallback(
    async (e: React.DragEvent) => {
      e.preventDefault();
      const files = Array.from(e.dataTransfer.files).filter((f) =>
        f.name.endsWith(".json")
      );
      if (files.length === 1) {
        const content = await files[0].text();
        const name = files[0].name.replace(/\.json$/i, "");
        onUpload(name, content);
      } else if (files.length > 1) {
        const results: { name: string; content: string }[] = [];
        for (const file of files) {
          const content = await file.text();
          results.push({ name: file.name.replace(/\.json$/i, ""), content });
        }
        onBulkUpload(results);
      }
    },
    [onUpload, onBulkUpload]
  );

  return (
    <div
      className="w-[260px] shrink-0 border-r border-white/[0.06] flex flex-col h-full"
      onDragOver={(e) => e.preventDefault()}
      onDrop={handleDrop}
    >
      <div className="p-3 border-b border-white/[0.06]">
        <h2 className="text-sm font-semibold text-foreground mb-2">Templates</h2>
        <div className="flex gap-1">
          <Button
            variant="outline"
            size="xs"
            onClick={() => fileInputRef.current?.click()}
            className="flex-1 text-xs"
          >
            <Upload className="h-3 w-3 mr-1" />
            Upload
          </Button>
          <Button
            variant="outline"
            size="xs"
            onClick={() => bulkInputRef.current?.click()}
            className="flex-1 text-xs"
          >
            <FolderOpen className="h-3 w-3 mr-1" />
            Bulk
          </Button>
        </div>
        <input
          ref={fileInputRef}
          type="file"
          accept=".json"
          onChange={handleFileChange}
          className="hidden"
        />
        <input
          ref={bulkInputRef}
          type="file"
          accept=".json"
          multiple
          onChange={handleBulkChange}
          className="hidden"
        />
      </div>

      <ScrollArea className="flex-1">
        <div className="p-2">
          {loading ? (
            Array.from({ length: 5 }).map((_, i) => (
              <Skeleton key={i} className="h-12 mb-1 rounded-md" />
            ))
          ) : templates.length === 0 ? (
            <div className="text-center text-muted-foreground text-xs py-8">
              <FileJson className="h-8 w-8 mx-auto mb-2 opacity-40" />
              <p>No templates yet</p>
              <p className="mt-1">Upload a JSON file or drag & drop</p>
            </div>
          ) : (
            templates.map((t) => (
              <div
                key={t.id}
                onClick={() => onSelect(t.id)}
                className={cn(
                  "flex items-center justify-between px-2 py-2 rounded-md cursor-pointer transition-colors group/item mb-0.5",
                  selectedId === t.id
                    ? "bg-teal-400/10 border-l-2 border-teal-400"
                    : "hover:bg-white/[0.04]"
                )}
              >
                <div className="min-w-0 flex-1">
                  <p className="text-sm text-foreground truncate">{t.name}</p>
                  {t.topicName && (
                    <p className="text-[10px] text-muted-foreground font-mono truncate">
                      {t.topicName}
                    </p>
                  )}
                </div>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onDelete(t.id);
                  }}
                  className="opacity-0 group-hover/item:opacity-60 hover:!opacity-100 transition-opacity ml-2"
                  title="Delete template"
                >
                  <Trash2 className="h-3.5 w-3.5 text-red-400" />
                </button>
              </div>
            ))
          )}
        </div>
      </ScrollArea>

      <div className="p-2 border-t border-white/[0.06] text-[10px] text-muted-foreground text-center">
        {templates.length} template{templates.length !== 1 ? "s" : ""} · Drop JSON files here
      </div>
    </div>
  );
}
