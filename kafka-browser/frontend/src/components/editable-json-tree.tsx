"use client";

import { useState, useCallback } from "react";
import { cn } from "@/lib/utils";
import { ChevronRight, X, Plus } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

interface EditableJsonTreeProps {
  data: unknown;
  onChange: (data: unknown) => void;
  defaultExpanded?: number;
}

export function EditableJsonTree({
  data,
  onChange,
  defaultExpanded = 3,
}: EditableJsonTreeProps) {
  return (
    <div className="text-sm font-mono">
      <EditableTreeNode
        value={data}
        depth={0}
        defaultExpanded={defaultExpanded}
        onChange={onChange}
        onDelete={undefined}
        isLast={true}
      />
    </div>
  );
}

interface EditableTreeNodeProps {
  label?: string;
  value: unknown;
  depth: number;
  defaultExpanded: number;
  onChange: (value: unknown) => void;
  onDelete: (() => void) | undefined;
  onRenameKey?: (oldKey: string, newKey: string) => void;
  isLast?: boolean;
}

function EditableTreeNode({
  label,
  value,
  depth,
  defaultExpanded,
  onChange,
  onDelete,
  isLast = true,
}: EditableTreeNodeProps) {
  const isObject =
    value !== null && typeof value === "object" && !Array.isArray(value);
  const isArray = Array.isArray(value);
  const isExpandable = isObject || isArray;
  const [expanded, setExpanded] = useState(depth < defaultExpanded);

  if (!isExpandable) {
    return (
      <div className="flex items-center gap-1 py-[2px] pl-4 group/leaf">
        {label !== undefined && (
          <span className="text-sky-300 shrink-0">{label}: </span>
        )}
        <LeafEditor value={value} onChange={onChange} />
        {onDelete && (
          <button
            onClick={onDelete}
            className="ml-1 opacity-0 group-hover/leaf:opacity-60 hover:!opacity-100 transition-opacity"
            title="Remove field"
          >
            <X className="h-3 w-3 text-red-400" />
          </button>
        )}
      </div>
    );
  }

  const entries = isArray
    ? (value as unknown[]).map((v, i) => [String(i), v] as const)
    : Object.entries(value as Record<string, unknown>);

  const bracketOpen = isArray ? "[" : "{";
  const bracketClose = isArray ? "]" : "}";
  const summary = isArray
    ? `${entries.length} item${entries.length !== 1 ? "s" : ""}`
    : `${entries.length} key${entries.length !== 1 ? "s" : ""}`;

  const handleChildChange = (key: string, newValue: unknown) => {
    if (isArray) {
      const arr = [...(value as unknown[])];
      arr[Number(key)] = newValue;
      onChange(arr);
    } else {
      onChange({ ...(value as Record<string, unknown>), [key]: newValue });
    }
  };

  const handleChildDelete = (key: string) => {
    if (isArray) {
      const arr = [...(value as unknown[])];
      arr.splice(Number(key), 1);
      onChange(arr);
    } else {
      const obj = { ...(value as Record<string, unknown>) };
      delete obj[key];
      onChange(obj);
    }
  };

  const handleAddField = () => {
    if (isArray) {
      onChange([...(value as unknown[]), ""]);
    } else {
      const obj = value as Record<string, unknown>;
      let newKey = "newField";
      let counter = 1;
      while (newKey in obj) {
        newKey = `newField${counter++}`;
      }
      onChange({ ...obj, [newKey]: "" });
    }
  };

  return (
    <div className="select-text">
      <div
        className={cn(
          "flex items-center gap-1 py-[2px] cursor-pointer rounded-sm hover:bg-white/[0.04] transition-colors group/node",
          depth > 0 && "pl-4"
        )}
        onClick={() => setExpanded((e) => !e)}
      >
        <ChevronRight
          className={cn(
            "h-3.5 w-3.5 shrink-0 text-muted-foreground transition-transform duration-150",
            expanded && "rotate-90"
          )}
        />
        {label !== undefined && (
          <span className="text-sky-300">{label}: </span>
        )}
        <span className="text-muted-foreground">
          {bracketOpen}
          {!expanded && (
            <span className="text-muted-foreground/60 text-xs ml-1">
              {summary}
            </span>
          )}
          {!expanded && bracketClose}
        </span>
        {onDelete && (
          <button
            onClick={(e) => {
              e.stopPropagation();
              onDelete();
            }}
            className="ml-1 opacity-0 group-hover/node:opacity-60 hover:!opacity-100 transition-opacity"
            title="Remove field"
          >
            <X className="h-3 w-3 text-red-400" />
          </button>
        )}
      </div>

      {expanded && (
        <>
          <div className="border-l border-white/[0.06] ml-[7px]">
            {entries.map(([key, val], i) => (
              <EditableTreeNode
                key={`${key}-${i}`}
                label={isArray ? undefined : key}
                value={val}
                depth={depth + 1}
                defaultExpanded={defaultExpanded}
                onChange={(newVal) => handleChildChange(key, newVal)}
                onDelete={() => handleChildDelete(key)}
                isLast={i === entries.length - 1}
              />
            ))}
            <div className="pl-4 py-1">
              <Button
                variant="ghost"
                size="xs"
                onClick={handleAddField}
                className="h-5 text-xs text-muted-foreground hover:text-teal-400"
              >
                <Plus className="h-3 w-3 mr-1" />
                {isArray ? "Add item" : "Add field"}
              </Button>
            </div>
          </div>
          <div className={cn("py-[1px]", depth > 0 && "pl-4")}>
            <span className="text-muted-foreground">{bracketClose}</span>
          </div>
        </>
      )}
    </div>
  );
}

function LeafEditor({
  value,
  onChange,
}: {
  value: unknown;
  onChange: (value: unknown) => void;
}) {
  const stringValue = value === null ? "null" : String(value);
  const [editValue, setEditValue] = useState(stringValue);

  const handleBlur = () => {
    if (editValue === "null") {
      onChange(null);
    } else if (editValue === "true") {
      onChange(true);
    } else if (editValue === "false") {
      onChange(false);
    } else if (editValue !== "" && !isNaN(Number(editValue))) {
      onChange(Number(editValue));
    } else {
      onChange(editValue);
    }
  };

  const colorClass =
    value === null
      ? "text-zinc-500"
      : typeof value === "boolean"
        ? "text-violet-400"
        : typeof value === "number"
          ? "text-amber-400"
          : "text-emerald-400";

  return (
    <Input
      value={editValue}
      onChange={(e) => setEditValue(e.target.value)}
      onBlur={handleBlur}
      className={cn(
        "h-5 px-1 py-0 text-xs border-none bg-transparent focus-visible:bg-white/[0.06] focus-visible:ring-1 focus-visible:ring-teal-400/50 rounded-sm",
        colorClass
      )}
      style={{ width: `${Math.max(editValue.length + 2, 6)}ch` }}
    />
  );
}
