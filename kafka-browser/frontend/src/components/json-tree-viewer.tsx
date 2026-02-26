"use client";

import { useState, useCallback } from "react";
import { cn } from "@/lib/utils";
import { ChevronRight, Copy, Check } from "lucide-react";

interface JsonTreeViewerProps {
  data: unknown;
  defaultExpanded?: number;
}

export function JsonTreeViewer({
  data,
  defaultExpanded = 2,
}: JsonTreeViewerProps) {
  if (data === null || data === undefined) {
    return <span className="text-muted-foreground italic">null</span>;
  }

  if (typeof data === "string") {
    try {
      const parsed = JSON.parse(data);
      return <TreeNode value={parsed} depth={0} defaultExpanded={defaultExpanded} />;
    } catch {
      return <span className="text-emerald-400">&quot;{data}&quot;</span>;
    }
  }

  return <TreeNode value={data} depth={0} defaultExpanded={defaultExpanded} />;
}

interface TreeNodeProps {
  label?: string;
  value: unknown;
  depth: number;
  defaultExpanded: number;
  isLast?: boolean;
}

function TreeNode({
  label,
  value,
  depth,
  defaultExpanded,
  isLast = true,
}: TreeNodeProps) {
  const isObject = value !== null && typeof value === "object" && !Array.isArray(value);
  const isArray = Array.isArray(value);
  const isExpandable = isObject || isArray;
  const [expanded, setExpanded] = useState(depth < defaultExpanded);

  if (!isExpandable) {
    return (
      <div className="flex items-start gap-1 py-[1px] pl-4 group/leaf">
        {label !== undefined && (
          <span className="text-sky-300 shrink-0">&quot;{label}&quot;: </span>
        )}
        <ValueRenderer value={value} />
        {!isLast && <span className="text-muted-foreground">,</span>}
        <CopyButton value={String(value)} />
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

  return (
    <div className="select-text">
      <div
        className={cn(
          "flex items-center gap-1 py-[1px] cursor-pointer rounded-sm hover:bg-white/[0.04] transition-colors group/node",
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
          <span className="text-sky-300">&quot;{label}&quot;: </span>
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
        {!expanded && !isLast && (
          <span className="text-muted-foreground">,</span>
        )}
      </div>

      {expanded && (
        <>
          <div className="border-l border-white/[0.06] ml-[7px]">
            {entries.map(([key, val], i) => (
              <TreeNode
                key={key}
                label={isArray ? undefined : key}
                value={val}
                depth={depth + 1}
                defaultExpanded={defaultExpanded}
                isLast={i === entries.length - 1}
              />
            ))}
            {entries.length === 0 && (
              <div className="pl-4 py-[1px] text-muted-foreground/50 text-xs italic">
                empty
              </div>
            )}
          </div>
          <div className={cn("py-[1px]", depth > 0 && "pl-4")}>
            <span className="text-muted-foreground">{bracketClose}</span>
            {!isLast && <span className="text-muted-foreground">,</span>}
          </div>
        </>
      )}
    </div>
  );
}

function ValueRenderer({ value }: { value: unknown }) {
  if (value === null || value === undefined) {
    return <span className="text-zinc-500 italic">null</span>;
  }
  if (typeof value === "boolean") {
    return (
      <span className="text-violet-400">{value ? "true" : "false"}</span>
    );
  }
  if (typeof value === "number") {
    return <span className="text-amber-400">{value}</span>;
  }
  if (typeof value === "string") {
    return <span className="text-emerald-400">&quot;{value}&quot;</span>;
  }
  return <span className="text-foreground">{String(value)}</span>;
}

function CopyButton({ value }: { value: string }) {
  const [copied, setCopied] = useState(false);

  const copy = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      navigator.clipboard.writeText(value).then(() => {
        setCopied(true);
        setTimeout(() => setCopied(false), 1500);
      });
    },
    [value]
  );

  return (
    <button
      onClick={copy}
      className="ml-1 opacity-0 group-hover/leaf:opacity-60 hover:!opacity-100 transition-opacity"
      title="Copy value"
    >
      {copied ? (
        <Check className="h-3 w-3 text-emerald-400" />
      ) : (
        <Copy className="h-3 w-3 text-muted-foreground" />
      )}
    </button>
  );
}
