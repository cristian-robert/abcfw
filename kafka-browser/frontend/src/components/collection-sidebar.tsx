"use client";

import { useState } from "react";
import { cn } from "@/lib/utils";
import { CollectionSummary } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Skeleton } from "@/components/ui/skeleton";
import { Plus, FolderOpen, Folder, Trash2, Upload } from "lucide-react";

interface CollectionSidebarProps {
  collections: CollectionSummary[];
  loading: boolean;
  selectedId: number | null;
  onSelect: (id: number) => void;
  onCreate: (name: string) => void;
  onDelete: (id: number) => void;
  onUploadAvsc: (id: number, content: string) => void;
}

export function CollectionSidebar({
  collections,
  loading,
  selectedId,
  onSelect,
  onCreate,
  onDelete,
  onUploadAvsc,
}: CollectionSidebarProps) {
  const [creating, setCreating] = useState(false);
  const [newName, setNewName] = useState("");

  const handleCreate = () => {
    if (newName.trim()) {
      onCreate(newName.trim());
      setNewName("");
      setCreating(false);
    }
  };

  const handleAvscUpload = (collectionId: number) => {
    const input = document.createElement("input");
    input.type = "file";
    input.accept = ".avsc,.json";
    input.onchange = async (e) => {
      const file = (e.target as HTMLInputElement).files?.[0];
      if (file) {
        const content = await file.text();
        onUploadAvsc(collectionId, content);
      }
    };
    input.click();
  };

  return (
    <div className="flex flex-col h-full border-r border-white/[0.06] w-[180px] shrink-0">
      <div className="flex items-center justify-between px-3 h-10 border-b border-white/[0.06] shrink-0">
        <span className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
          Collections
        </span>
        <Button
          variant="ghost"
          size="icon"
          className="h-6 w-6"
          onClick={() => setCreating(true)}
        >
          <Plus className="h-3.5 w-3.5" />
        </Button>
      </div>

      <ScrollArea className="flex-1 min-h-0">
        <div className="p-1.5">
          {loading && (
            <div className="space-y-1">
              {Array.from({ length: 4 }).map((_, i) => (
                <Skeleton key={i} className="h-8 w-full rounded-md" />
              ))}
            </div>
          )}

          {!loading &&
            collections.map((col) => {
              const isSelected = col.id === selectedId;
              return (
                <div
                  key={col.id}
                  className={cn(
                    "group flex items-center gap-1.5 px-2 py-1.5 rounded-md cursor-pointer text-sm transition-colors",
                    isSelected
                      ? "bg-white/[0.08] text-teal-400"
                      : "text-muted-foreground hover:text-foreground hover:bg-white/[0.04]"
                  )}
                  onClick={() => onSelect(col.id)}
                >
                  {isSelected ? (
                    <FolderOpen className="h-3.5 w-3.5 shrink-0" />
                  ) : (
                    <Folder className="h-3.5 w-3.5 shrink-0" />
                  )}
                  <span className="truncate flex-1">{col.name}</span>
                  <span className="text-[10px] text-muted-foreground/60">
                    {col.templateCount}
                  </span>
                  <div className="hidden group-hover:flex items-center gap-0.5">
                    <button
                      className="p-0.5 rounded hover:bg-white/[0.08]"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleAvscUpload(col.id);
                      }}
                      title="Upload .avsc"
                    >
                      <Upload className="h-3 w-3" />
                    </button>
                    <button
                      className="p-0.5 rounded hover:bg-red-500/20 text-red-400"
                      onClick={(e) => {
                        e.stopPropagation();
                        onDelete(col.id);
                      }}
                      title="Delete collection"
                    >
                      <Trash2 className="h-3 w-3" />
                    </button>
                  </div>
                </div>
              );
            })}

          {creating && (
            <div className="p-1">
              <Input
                autoFocus
                placeholder="Collection name"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") handleCreate();
                  if (e.key === "Escape") {
                    setCreating(false);
                    setNewName("");
                  }
                }}
                onBlur={() => {
                  if (!newName.trim()) setCreating(false);
                }}
                className="h-7 text-xs"
              />
            </div>
          )}
        </div>
      </ScrollArea>
    </div>
  );
}
