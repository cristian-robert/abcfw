"use client";

import { useState, useCallback } from "react";
import { KafkaMessage } from "@/lib/types";
import { cn, formatTimestamp, formatOffset } from "@/lib/utils";
import { JsonTreeViewer } from "./json-tree-viewer";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import {
  ChevronRight,
  AlertCircle,
  Braces,
  Tag,
  Info,
  Code2,
  Copy,
  Check,
  Clock,
  Hash,
} from "lucide-react";

interface MessageCardProps {
  message: KafkaMessage;
  index: number;
}

const TAB_CLASS =
  "text-xs h-8 gap-1.5 px-3 rounded-none border-b-2 border-transparent data-[state=active]:border-teal-400 data-[state=active]:text-teal-300 data-[state=active]:bg-transparent data-[state=active]:shadow-none transition-colors duration-200 text-muted-foreground/50 hover:text-muted-foreground/80";

export function MessageCard({ message, index }: MessageCardProps) {
  const [open, setOpen] = useState(index === 0);
  const headerCount = message.headers
    ? Object.keys(message.headers).length
    : 0;

  return (
    <div
      className="animate-card-enter"
      style={{ animationDelay: `${index * 40}ms` }}
    >
      <Collapsible open={open} onOpenChange={setOpen}>
        <div
          className={cn(
            "rounded-lg overflow-hidden transition-colors duration-200",
            "bg-[#111113] ring-1",
            open
              ? "ring-white/[0.12] glow-teal"
              : "ring-white/[0.06] hover:ring-white/[0.1] hover:bg-[#131315]"
          )}
        >
          <CollapsibleTrigger className="w-full cursor-pointer">
            <div
              className={cn(
                "flex items-center gap-3 px-4 py-2.5 text-xs text-left",
                "transition-colors duration-200",
                open && "border-b border-white/[0.06]"
              )}
            >
              <ChevronRight
                aria-hidden="true"
                className={cn(
                  "h-3.5 w-3.5 shrink-0 text-muted-foreground/60 transition-transform duration-200",
                  open && "rotate-90 text-teal-400/60"
                )}
              />

              <div className="flex items-center gap-2 min-w-0 flex-1">
                <Badge
                  variant="secondary"
                  className="font-mono text-[10px] h-4 px-1.5 shrink-0"
                >
                  P:{message.partition}
                </Badge>
                <Badge
                  variant="secondary"
                  className="font-mono text-[10px] h-4 px-1.5 shrink-0"
                >
                  O:{formatOffset(message.offset)}
                </Badge>
                <span className="text-muted-foreground/40 shrink-0 tabular-nums flex items-center gap-1">
                  <Clock className="h-3 w-3" aria-hidden="true" />
                  {formatTimestamp(message.timestamp)}
                </span>

                {message.schemaName && (
                  <Badge
                    variant="outline"
                    className="text-[10px] h-4 px-1.5 border-teal-500/30 text-teal-400/80 font-mono shrink-0"
                  >
                    {message.schemaName}
                  </Badge>
                )}

                {message.error && (
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <AlertCircle
                        className="h-3.5 w-3.5 text-destructive shrink-0"
                        aria-label="Message has error"
                      />
                    </TooltipTrigger>
                    <TooltipContent side="top" className="text-xs max-w-[300px]">
                      {message.error}
                    </TooltipContent>
                  </Tooltip>
                )}
              </div>

              {message.key && (
                <Tooltip>
                  <TooltipTrigger asChild>
                    <span className="text-muted-foreground/30 font-mono text-[10px] truncate max-w-[120px] flex items-center gap-1">
                      <Hash className="h-2.5 w-2.5 shrink-0" aria-hidden="true" />
                      {message.key}
                    </span>
                  </TooltipTrigger>
                  <TooltipContent side="top" className="font-mono text-xs">
                    Key: {message.key}
                  </TooltipContent>
                </Tooltip>
              )}
            </div>
          </CollapsibleTrigger>

          <CollapsibleContent>
            <Tabs defaultValue="message" className="w-full">
              <TabsList className="w-full justify-start rounded-none border-b border-white/[0.06] bg-transparent h-9 px-2 gap-0">
                <TabsTrigger value="message" className={TAB_CLASS}>
                  <Braces className="h-3 w-3" aria-hidden="true" />
                  Message
                </TabsTrigger>
                <TabsTrigger value="headers" className={TAB_CLASS}>
                  <Tag className="h-3 w-3" aria-hidden="true" />
                  Headers
                  {headerCount > 0 && (
                    <span className="text-[9px] font-mono bg-white/[0.06] text-muted-foreground/60 px-1 py-px rounded ml-0.5">
                      {headerCount}
                    </span>
                  )}
                </TabsTrigger>
                <TabsTrigger value="metadata" className={TAB_CLASS}>
                  <Info className="h-3 w-3" aria-hidden="true" />
                  Metadata
                </TabsTrigger>
                <TabsTrigger value="raw" className={TAB_CLASS}>
                  <Code2 className="h-3 w-3" aria-hidden="true" />
                  Raw
                </TabsTrigger>
              </TabsList>

              <div className="max-h-[600px] overflow-auto">
                <TabsContent value="message" className="m-0 p-4">
                  <MessageContent message={message} />
                </TabsContent>

                <TabsContent value="headers" className="m-0 p-4">
                  <HeadersContent headers={message.headers} />
                </TabsContent>

                <TabsContent value="metadata" className="m-0 p-4">
                  <MetadataContent message={message} />
                </TabsContent>

                <TabsContent value="raw" className="m-0 p-4">
                  <RawContent rawMessage={message.rawMessage} />
                </TabsContent>
              </div>
            </Tabs>
          </CollapsibleContent>
        </div>
      </Collapsible>
    </div>
  );
}

/* ─── Tab content sub-components ─── */

function MessageContent({ message }: { message: KafkaMessage }) {
  if (message.error) {
    return (
      <div className="flex items-start gap-2.5 text-xs text-destructive bg-destructive/10 rounded-lg p-3 ring-1 ring-destructive/20">
        <AlertCircle
          className="h-3.5 w-3.5 shrink-0 mt-0.5"
          aria-hidden="true"
        />
        <span>{message.error}</span>
      </div>
    );
  }

  if (message.message) {
    return (
      <div className="font-mono text-xs leading-relaxed">
        <JsonTreeViewer data={message.message} defaultExpanded={3} />
      </div>
    );
  }

  if (message.rawMessage) {
    return (
      <pre className="text-xs font-mono text-muted-foreground whitespace-pre-wrap break-all">
        {message.rawMessage}
      </pre>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center py-10 text-muted-foreground/30">
      <Braces className="h-6 w-6 mb-2 stroke-[1.2]" aria-hidden="true" />
      <span className="text-xs">No message body</span>
    </div>
  );
}

function HeadersContent({ headers }: { headers: Record<string, string> }) {
  if (!headers || Object.keys(headers).length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-10 text-muted-foreground/30">
        <Tag className="h-6 w-6 mb-2 stroke-[1.2]" aria-hidden="true" />
        <span className="text-xs">No headers</span>
      </div>
    );
  }

  return (
    <div className="rounded-lg ring-1 ring-white/[0.06] overflow-hidden">
      {Object.entries(headers).map(([key, val], i) => (
        <div
          key={key}
          className={cn(
            "flex items-start gap-4 text-xs font-mono px-3 py-2.5",
            "transition-colors duration-150",
            i % 2 === 0 ? "bg-white/[0.02]" : "bg-transparent",
            "hover:bg-white/[0.04]"
          )}
        >
          <span className="text-sky-300 shrink-0 min-w-[140px] select-all">
            {key}
          </span>
          <span className="text-muted-foreground break-all select-all">
            {val}
          </span>
        </div>
      ))}
    </div>
  );
}

function MetadataContent({ message }: { message: KafkaMessage }) {
  const fields = [
    { label: "Topic", value: message.topic, mono: true },
    { label: "Partition", value: String(message.partition) },
    { label: "Offset", value: formatOffset(message.offset) },
    { label: "Timestamp", value: formatTimestamp(message.timestamp) },
    { label: "Key", value: message.key ?? "—", mono: true },
    {
      label: "Schema ID",
      value: message.schemaId != null ? String(message.schemaId) : "—",
    },
    { label: "Schema Type", value: message.schemaType ?? "—" },
    { label: "Schema Name", value: message.schemaName ?? "—", mono: true },
  ];

  return (
    <div className="grid grid-cols-2 gap-2.5">
      {fields.map(({ label, value, mono }) => (
        <div
          key={label}
          className="flex flex-col gap-1 px-3 py-2.5 rounded-lg bg-white/[0.02] ring-1 ring-white/[0.04] hover:ring-white/[0.08] transition-colors duration-150"
        >
          <span className="text-[10px] text-muted-foreground/40 uppercase tracking-wider font-medium">
            {label}
          </span>
          <span
            className={cn(
              "text-xs text-foreground/80 break-all select-all",
              mono && "font-mono",
              value === "—" && "text-muted-foreground/30"
            )}
          >
            {value}
          </span>
        </div>
      ))}
    </div>
  );
}

function RawContent({ rawMessage }: { rawMessage: string | null }) {
  const [copied, setCopied] = useState(false);

  const copy = useCallback(() => {
    if (!rawMessage) return;
    navigator.clipboard.writeText(rawMessage).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    });
  }, [rawMessage]);

  if (!rawMessage) {
    return (
      <div className="flex flex-col items-center justify-center py-10 text-muted-foreground/30">
        <Code2 className="h-6 w-6 mb-2 stroke-[1.2]" aria-hidden="true" />
        <span className="text-xs">No raw message</span>
      </div>
    );
  }

  return (
    <div className="relative group/raw">
      <button
        type="button"
        onClick={copy}
        className={cn(
          "absolute top-2 right-2 p-1.5 rounded-md transition-all duration-150",
          "opacity-0 group-hover/raw:opacity-70 hover:!opacity-100",
          "bg-white/[0.06] hover:bg-white/[0.1]",
          copied && "!opacity-100"
        )}
        aria-label="Copy raw message"
      >
        {copied ? (
          <Check
            className="h-3.5 w-3.5 text-emerald-400"
            aria-hidden="true"
          />
        ) : (
          <Copy
            className="h-3.5 w-3.5 text-muted-foreground"
            aria-hidden="true"
          />
        )}
      </button>
      <pre className="text-xs font-mono text-muted-foreground whitespace-pre-wrap break-all bg-white/[0.02] rounded-lg p-3 ring-1 ring-white/[0.04] select-all">
        {rawMessage}
      </pre>
    </div>
  );
}
