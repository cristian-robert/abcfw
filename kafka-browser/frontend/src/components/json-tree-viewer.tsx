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
  "border-teal-500/25",
  "border-sky-500/25",
  "border-violet-500/25",
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
        <div className="flex items-center gap-1 mb-2">
          <button
            onClick={() => {
              setExpandLevel(Infinity);
              setResetKey((k) => k + 1);
            }}
            className="flex items-center gap-1 text-[10px] text-muted-foreground/60 hover:text-muted-foreground transition-all duration-200 px-1.5 py-0.5 rounded hover:bg-white/[0.06] hover:shadow-[0_0_8px_rgba(45,212,191,0.08)]"
          >
            <Maximize2 className="h-3 w-3" />
            Expand all
          </button>
          <button
            onClick={() => {
              setExpandLevel(1);
              setResetKey((k) => k + 1);
            }}
            className="flex items-center gap-1 text-[10px] text-muted-foreground/60 hover:text-muted-foreground transition-all duration-200 px-1.5 py-0.5 rounded hover:bg-white/[0.06] hover:shadow-[0_0_8px_rgba(45,212,191,0.08)]"
          >
            <Minimize2 className="h-3 w-3" />
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
      <div className="flex items-start gap-1 py-0.5 pl-5 group/leaf">
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

  const guideColor = GUIDE_COLORS[depth % GUIDE_COLORS.length];

  return (
    <div className="select-text">
      <div
        className={cn(
          "flex items-center gap-1 py-0.5 cursor-pointer rounded-sm hover:bg-white/[0.04] transition-colors group/node",
          depth > 0 && "pl-5"
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
            <span className="inline-flex items-center ml-1.5">
              <span className="text-[10px] font-mono bg-white/[0.06] text-muted-foreground/80 px-1.5 py-px rounded">
                {summary}
              </span>
            </span>
          )}
          {!expanded && <span className="ml-0.5">{bracketClose}</span>}
        </span>
        {!expanded && !isLast && (
          <span className="text-muted-foreground">,</span>
        )}
        <CopyObjectButton value={value} />
      </div>

      {expanded && (
        <>
          <div
            className={cn(
              "border-l-2 ml-[7px]",
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
              <div className="pl-5 py-0.5 text-muted-foreground/50 text-xs italic">
                empty
              </div>
            )}
          </div>
          <div className={cn("py-0.5", depth > 0 && "pl-5")}>
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
      onClick={copy}
      className="ml-1 opacity-0 group-hover/node:opacity-60 hover:!opacity-100 transition-opacity"
      title="Copy object"
    >
      {copied ? (
        <Check className="h-3 w-3 text-emerald-400" />
      ) : (
        <Copy className="h-3 w-3 text-muted-foreground" />
      )}
    </button>
  );
}
