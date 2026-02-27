"use client";

import { useState, useCallback } from "react";
import { cn } from "@/lib/utils";
import {
  ChevronRight,
  Copy,
  Check,
  Maximize2,
  Minimize2,
} from "lucide-react";

interface JsonTreeViewerProps {
  data: unknown;
  defaultExpanded?: number;
}

const GUIDE_COLORS = [
  "border-teal-500/40",
  "border-sky-500/40",
  "border-violet-500/40",
];

export function JsonTreeViewer({
  data,
  defaultExpanded = 2,
}: JsonTreeViewerProps) {
  const [resetKey, setResetKey] = useState(0);
  const [expandLevel, setExpandLevel] = useState(defaultExpanded);

  if (data === null || data === undefined) {
    return <span className="text-muted-foreground italic">null</span>;
  }

  let parsedData = data;
  if (typeof data === "string") {
    try {
      parsedData = JSON.parse(data);
    } catch {
      return <span className="text-emerald-400">&quot;{data}&quot;</span>;
    }
  }

  const isExpandable = parsedData !== null && typeof parsedData === "object";

  return (
    <div>
      {isExpandable && (
        <div className="flex items-center gap-0.5 mb-2.5">
          <button
            type="button"
            onClick={() => {
              setExpandLevel(Infinity);
              setResetKey((k) => k + 1);
            }}
            className="flex items-center gap-1 text-[10px] text-muted-foreground/50 hover:text-muted-foreground hover:bg-white/[0.06] transition-colors duration-150 px-2 py-1 rounded-md"
            aria-label="Expand all tree nodes"
          >
            <Maximize2 className="h-3 w-3" aria-hidden="true" />
            Expand all
          </button>
          <button
            type="button"
            onClick={() => {
              setExpandLevel(1);
              setResetKey((k) => k + 1);
            }}
            className="flex items-center gap-1 text-[10px] text-muted-foreground/50 hover:text-muted-foreground hover:bg-white/[0.06] transition-colors duration-150 px-2 py-1 rounded-md"
            aria-label="Collapse all tree nodes"
          >
            <Minimize2 className="h-3 w-3" aria-hidden="true" />
            Collapse all
          </button>
        </div>
      )}
      <TreeNode
        key={resetKey}
        value={parsedData}
        depth={0}
        defaultExpanded={expandLevel}
      />
    </div>
  );
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
  const isObject =
    value !== null && typeof value === "object" && !Array.isArray(value);
  const isArray = Array.isArray(value);
  const isExpandable = isObject || isArray;
  const [expanded, setExpanded] = useState(depth < defaultExpanded);

  if (!isExpandable) {
    return (
      <div className="flex items-start gap-1 py-0.5 pl-5 group/leaf select-text hover:bg-white/[0.02] rounded-sm transition-colors duration-100">
        {label !== undefined && (
          <span className="text-sky-300 shrink-0">&quot;{label}&quot;: </span>
        )}
        <ValueRenderer value={value} />
        {!isLast && <span className="text-muted-foreground/50">,</span>}
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

  const guideColor = GUIDE_COLORS[depth % GUIDE_COLORS.length];

  return (
    <div>
      <div className="flex items-center group/node">
        <button
          type="button"
          className={cn(
            "flex items-center gap-1 py-1 cursor-pointer rounded-sm",
            "hover:bg-white/[0.04] transition-colors duration-150",
            "focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-teal-500/50",
            "select-none flex-1 min-w-0 text-left",
            depth > 0 && "pl-5"
          )}
          onClick={() => setExpanded((prev) => !prev)}
          aria-expanded={expanded}
          aria-label={
            label
              ? `${expanded ? "Collapse" : "Expand"} ${label}`
              : `${expanded ? "Collapse" : "Expand"} ${isArray ? "array" : "object"}`
          }
        >
          <ChevronRight
            aria-hidden="true"
            className={cn(
              "h-3.5 w-3.5 shrink-0 text-muted-foreground/50 transition-transform duration-200",
              expanded && "rotate-90 text-muted-foreground/70"
            )}
          />
          {label !== undefined && (
            <span className="text-sky-300">&quot;{label}&quot;: </span>
          )}
          <span className="text-muted-foreground/60">
            {bracketOpen}
            {!expanded && (
              <span className="inline-flex items-center ml-1.5">
                <span className="text-[10px] font-mono bg-white/[0.06] text-muted-foreground/60 px-1.5 py-px rounded">
                  {summary}
                </span>
              </span>
            )}
            {!expanded && <span className="ml-0.5">{bracketClose}</span>}
          </span>
          {!expanded && !isLast && (
            <span className="text-muted-foreground/50">,</span>
          )}
        </button>
        <CopyObjectButton value={value} />
      </div>

      {expanded && (
        <>
          <div
            className={cn(
              "border-l-2 ml-4",
              guideColor,
              depth % 2 === 0 && "bg-white/[0.015] rounded-r-sm"
            )}
          >
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
              <div className="pl-5 py-0.5 text-muted-foreground/30 text-xs italic">
                empty
              </div>
            )}
          </div>
          <div className={cn("py-0.5", depth > 0 && "pl-5")}>
            <span className="text-muted-foreground/60">{bracketClose}</span>
            {!isLast && <span className="text-muted-foreground/50">,</span>}
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
    if (value.length > 200) {
      return (
        <span className="text-emerald-400">
          &quot;{value.slice(0, 200)}
          <span className="text-emerald-400/50">...({value.length} chars)</span>
          &quot;
        </span>
      );
    }
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
      type="button"
      onClick={copy}
      className={cn(
        "ml-1 transition-opacity duration-150",
        copied
          ? "opacity-100"
          : "opacity-0 group-hover/leaf:opacity-40 hover:!opacity-100"
      )}
      aria-label="Copy value"
    >
      {copied ? (
        <Check className="h-3 w-3 text-emerald-400" aria-hidden="true" />
      ) : (
        <Copy className="h-3 w-3 text-muted-foreground" aria-hidden="true" />
      )}
    </button>
  );
}

function CopyObjectButton({ value }: { value: unknown }) {
  const [copied, setCopied] = useState(false);

  const copy = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      const json = JSON.stringify(value, null, 2);
      navigator.clipboard.writeText(json).then(() => {
        setCopied(true);
        setTimeout(() => setCopied(false), 1500);
      });
    },
    [value]
  );

  return (
    <button
      type="button"
      onClick={copy}
      className={cn(
        "ml-1 shrink-0 transition-opacity duration-150",
        copied
          ? "opacity-100"
          : "opacity-0 group-hover/node:opacity-40 hover:!opacity-100"
      )}
      aria-label="Copy object as JSON"
    >
      {copied ? (
        <Check className="h-3 w-3 text-emerald-400" aria-hidden="true" />
      ) : (
        <Copy className="h-3 w-3 text-muted-foreground" aria-hidden="true" />
      )}
    </button>
  );
}
