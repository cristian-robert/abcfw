"use client";

import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ChevronDown, ChevronRight, Plus, Trash2 } from "lucide-react";
import { cn } from "@/lib/utils";

type JsonValueType = "string" | "number" | "boolean" | "null" | "object" | "array";

function detectType(value: unknown): JsonValueType {
  if (value === null) return "null";
  if (Array.isArray(value)) return "array";
  if (typeof value === "object") return "object";
  if (typeof value === "boolean") return "boolean";
  if (typeof value === "number") return "number";
  return "string";
}

function defaultForType(type: JsonValueType): unknown {
  switch (type) {
    case "string": return "";
    case "number": return 0;
    case "boolean": return false;
    case "null": return null;
    case "object": return {};
    case "array": return [];
  }
}

interface FormFieldRendererProps {
  label: string;
  value: unknown;
  depth: number;
  onChange: (value: unknown) => void;
  onDelete?: () => void;
  onKeyRename?: (oldKey: string, newKey: string) => void;
}

export function FormFieldRenderer({
  label,
  value,
  depth,
  onChange,
  onDelete,
  onKeyRename,
}: FormFieldRendererProps) {
  const [expanded, setExpanded] = useState(depth < 2);
  const [editingKey, setEditingKey] = useState(false);
  const [keyDraft, setKeyDraft] = useState(label);

  const isArrayIndex = label.startsWith("[") && label.endsWith("]");

  const commitKeyRename = () => {
    const trimmed = keyDraft.trim();
    if (trimmed && trimmed !== label && onKeyRename) {
      onKeyRename(label, trimmed);
    }
    setEditingKey(false);
    setKeyDraft(trimmed || label);
  };

  const renderKeyLabel = () => {
    if (isArrayIndex || !onKeyRename) {
      return (
        <span
          className="text-xs text-muted-foreground shrink-0 truncate"
          style={{ width: depth > 0 ? "auto" : undefined, minWidth: "3rem", maxWidth: "10rem" }}
          title={label}
        >
          {label}
        </span>
      );
    }

    if (editingKey) {
      return (
        <Input
          autoFocus
          value={keyDraft}
          onChange={(e) => setKeyDraft(e.target.value)}
          onBlur={commitKeyRename}
          onKeyDown={(e) => {
            if (e.key === "Enter") commitKeyRename();
            if (e.key === "Escape") {
              setKeyDraft(label);
              setEditingKey(false);
            }
          }}
          className="h-5 text-xs w-28 px-1 py-0"
          onClick={(e) => e.stopPropagation()}
        />
      );
    }

    return (
      <span
        className="text-xs text-muted-foreground shrink-0 truncate cursor-pointer hover:text-foreground border-b border-dashed border-transparent hover:border-muted-foreground/30"
        style={{ minWidth: "3rem", maxWidth: "10rem" }}
        title={`${label} (click to rename)`}
        onDoubleClick={(e) => {
          e.stopPropagation();
          setKeyDraft(label);
          setEditingKey(true);
        }}
      >
        {label}
      </span>
    );
  };

  // Object
  if (value !== null && typeof value === "object" && !Array.isArray(value)) {
    const obj = value as Record<string, unknown>;
    const keys = Object.keys(obj);

    const handleChildKeyRename = (oldKey: string, newKey: string) => {
      if (oldKey === newKey || newKey in obj) return;
      const newObj: Record<string, unknown> = {};
      for (const k of keys) {
        if (k === oldKey) {
          newObj[newKey] = obj[oldKey];
        } else {
          newObj[k] = obj[k];
        }
      }
      onChange(newObj);
    };

    return (
      <div
        className={cn("border-l border-white/[0.06]", depth > 0 && "ml-3")}
      >
        <div
          className="group flex items-center gap-1 py-1 px-1 cursor-pointer hover:bg-white/[0.02] rounded-sm"
          onClick={() => setExpanded(!expanded)}
        >
          {expanded ? (
            <ChevronDown className="h-3 w-3 text-muted-foreground shrink-0" />
          ) : (
            <ChevronRight className="h-3 w-3 text-muted-foreground shrink-0" />
          )}
          {renderKeyLabel()}
          <span className="text-[10px] text-muted-foreground/40">{`{${keys.length}}`}</span>
          {onDelete && (
            <button
              className="hidden group-hover:block ml-auto p-0.5 rounded hover:bg-red-500/20 text-red-400"
              onClick={(e) => {
                e.stopPropagation();
                onDelete();
              }}
            >
              <Trash2 className="h-3 w-3" />
            </button>
          )}
        </div>
        {expanded && (
          <div className="pl-2">
            {keys.map((key) => (
              <FormFieldRenderer
                key={key}
                label={key}
                value={obj[key]}
                depth={depth + 1}
                onChange={(v) => onChange({ ...obj, [key]: v })}
                onDelete={() => {
                  const next = { ...obj };
                  delete next[key];
                  onChange(next);
                }}
                onKeyRename={handleChildKeyRename}
              />
            ))}
            <Button
              variant="ghost"
              size="sm"
              className="h-6 text-[10px] text-muted-foreground mt-1"
              onClick={() => {
                let key = "newField";
                let i = 0;
                while (key in obj) {
                  i++;
                  key = `newField${i}`;
                }
                onChange({ ...obj, [key]: "" });
              }}
            >
              <Plus className="h-3 w-3 mr-1" /> Add Field
            </Button>
          </div>
        )}
      </div>
    );
  }

  // Array
  if (Array.isArray(value)) {
    return (
      <div
        className={cn("border-l border-white/[0.06]", depth > 0 && "ml-3")}
      >
        <div
          className="group flex items-center gap-1 py-1 px-1 cursor-pointer hover:bg-white/[0.02] rounded-sm"
          onClick={() => setExpanded(!expanded)}
        >
          {expanded ? (
            <ChevronDown className="h-3 w-3 text-muted-foreground shrink-0" />
          ) : (
            <ChevronRight className="h-3 w-3 text-muted-foreground shrink-0" />
          )}
          {renderKeyLabel()}
          <span className="text-[10px] text-muted-foreground/40">{`[${value.length}]`}</span>
          {onDelete && (
            <button
              className="hidden group-hover:block ml-auto p-0.5 rounded hover:bg-red-500/20 text-red-400"
              onClick={(e) => {
                e.stopPropagation();
                onDelete();
              }}
            >
              <Trash2 className="h-3 w-3" />
            </button>
          )}
        </div>
        {expanded && (
          <div className="pl-2">
            {value.map((item, i) => (
              <FormFieldRenderer
                key={i}
                label={`[${i}]`}
                value={item}
                depth={depth + 1}
                onChange={(v) => {
                  const next = [...value];
                  next[i] = v;
                  onChange(next);
                }}
                onDelete={() => {
                  const next = value.filter((_, idx) => idx !== i);
                  onChange(next);
                }}
              />
            ))}
            <Button
              variant="ghost"
              size="sm"
              className="h-6 text-[10px] text-muted-foreground mt-1"
              onClick={() => onChange([...value, ""])}
            >
              <Plus className="h-3 w-3 mr-1" /> Add Item
            </Button>
          </div>
        )}
      </div>
    );
  }

  // Scalar value (string, number, boolean, null)
  const currentType = detectType(value);

  return (
    <div
      className={cn(
        "group flex items-center gap-2 py-0.5 px-1",
        depth > 0 && "ml-3"
      )}
    >
      {renderKeyLabel()}

      <Select
        value={currentType}
        onValueChange={(newType) => {
          onChange(defaultForType(newType as JsonValueType));
        }}
      >
        <SelectTrigger className="h-5 w-[62px] text-[10px] px-1.5 bg-white/[0.04] border-white/[0.06] shrink-0">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="string">str</SelectItem>
          <SelectItem value="number">num</SelectItem>
          <SelectItem value="boolean">bool</SelectItem>
          <SelectItem value="null">null</SelectItem>
          <SelectItem value="object">obj</SelectItem>
          <SelectItem value="array">arr</SelectItem>
        </SelectContent>
      </Select>

      {currentType === "null" ? (
        <span className="text-xs text-zinc-500 italic flex-1">null</span>
      ) : currentType === "boolean" ? (
        <input
          type="checkbox"
          checked={value as boolean}
          onChange={(e) => onChange(e.target.checked)}
          className="h-3.5 w-3.5 accent-teal-500"
        />
      ) : currentType === "number" ? (
        <Input
          type="number"
          value={value as number}
          onChange={(e) => {
            const v = e.target.value;
            onChange(v === "" ? 0 : Number(v));
          }}
          className="h-7 text-xs flex-1"
        />
      ) : (
        <Input
          value={String(value ?? "")}
          onChange={(e) => onChange(e.target.value)}
          placeholder="(empty)"
          className="h-7 text-xs flex-1"
        />
      )}

      {onDelete && (
        <button
          className="hidden group-hover:block p-0.5 rounded hover:bg-red-500/20 text-red-400 shrink-0"
          onClick={onDelete}
        >
          <Trash2 className="h-3 w-3" />
        </button>
      )}
    </div>
  );
}
