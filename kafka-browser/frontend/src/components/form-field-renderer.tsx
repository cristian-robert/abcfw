"use client";

import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { ChevronDown, ChevronRight, Plus, Trash2 } from "lucide-react";
import { cn } from "@/lib/utils";

interface FormFieldRendererProps {
  label: string;
  value: unknown;
  depth: number;
  onChange: (value: unknown) => void;
  onDelete?: () => void;
}

export function FormFieldRenderer({
  label,
  value,
  depth,
  onChange,
  onDelete,
}: FormFieldRendererProps) {
  const [expanded, setExpanded] = useState(depth < 2);

  if (value !== null && typeof value === "object" && !Array.isArray(value)) {
    const obj = value as Record<string, unknown>;
    const keys = Object.keys(obj);
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
          <span className="text-xs font-medium text-muted-foreground">
            {label}
          </span>
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
              />
            ))}
            <Button
              variant="ghost"
              size="sm"
              className="h-6 text-[10px] text-muted-foreground mt-1"
              onClick={() => {
                const key = `newField${keys.length}`;
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
          <span className="text-xs font-medium text-muted-foreground">
            {label}
          </span>
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

  // Scalar value
  return (
    <div
      className={cn(
        "group flex items-center gap-2 py-0.5 px-1",
        depth > 0 && "ml-3"
      )}
    >
      <label
        className="text-xs text-muted-foreground w-28 shrink-0 truncate"
        title={label}
      >
        {label}
      </label>
      {typeof value === "boolean" ? (
        <input
          type="checkbox"
          checked={value}
          onChange={(e) => onChange(e.target.checked)}
          className="h-3.5 w-3.5 accent-teal-500"
        />
      ) : typeof value === "number" ? (
        <Input
          type="number"
          value={value}
          onChange={(e) => {
            const v = e.target.value;
            onChange(v === "" ? 0 : Number(v));
          }}
          className="h-7 text-xs flex-1"
        />
      ) : (
        <Input
          value={value === null ? "" : String(value)}
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
