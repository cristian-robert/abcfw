"use client";

import { useState } from "react";
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
import { ChevronRight, AlertCircle } from "lucide-react";

interface MessageCardProps {
  message: KafkaMessage;
  index: number;
}

export function MessageCard({ message, index }: MessageCardProps) {
  const [open, setOpen] = useState(index === 0);

  return (
    <Collapsible open={open} onOpenChange={setOpen}>
      <div
        className={cn(
          "border border-white/[0.06] rounded-lg overflow-hidden transition-colors",
          "bg-white/[0.02] hover:bg-white/[0.03]",
          open && "bg-white/[0.03]"
        )}
        style={{ animationDelay: `${index * 30}ms` }}
      >
        <CollapsibleTrigger className="w-full">
          <div className="flex items-center gap-3 px-4 py-2.5 text-xs cursor-pointer">
            <ChevronRight
              className={cn(
                "h-3.5 w-3.5 shrink-0 text-muted-foreground transition-transform duration-150",
                open && "rotate-90"
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
              <span className="text-muted-foreground/60 shrink-0">
                {formatTimestamp(message.timestamp)}
              </span>

              {message.schemaName && (
                <span className="text-teal-400/70 truncate font-mono">
                  {message.schemaName}
                </span>
              )}

              {message.error && (
                <AlertCircle className="h-3.5 w-3.5 text-destructive shrink-0" />
              )}
            </div>

            {message.key && (
              <span className="text-muted-foreground/40 font-mono text-[10px] truncate max-w-[120px]">
                key={message.key}
              </span>
            )}
          </div>
        </CollapsibleTrigger>

        <CollapsibleContent>
          <div className="border-t border-white/[0.06]">
            <Tabs defaultValue="message" className="w-full">
              <TabsList className="w-full justify-start rounded-none border-b border-white/[0.06] bg-transparent h-8 px-4">
                <TabsTrigger
                  value="message"
                  className="text-xs h-7 rounded-sm data-[state=active]:bg-white/[0.06]"
                >
                  Message
                </TabsTrigger>
                <TabsTrigger
                  value="headers"
                  className="text-xs h-7 rounded-sm data-[state=active]:bg-white/[0.06]"
                >
                  Headers
                  {message.headers && Object.keys(message.headers).length > 0 && (
                    <Badge variant="secondary" className="ml-1.5 h-3.5 text-[9px] px-1">
                      {Object.keys(message.headers).length}
                    </Badge>
                  )}
                </TabsTrigger>
                <TabsTrigger
                  value="metadata"
                  className="text-xs h-7 rounded-sm data-[state=active]:bg-white/[0.06]"
                >
                  Metadata
                </TabsTrigger>
                <TabsTrigger
                  value="raw"
                  className="text-xs h-7 rounded-sm data-[state=active]:bg-white/[0.06]"
                >
                  Raw
                </TabsTrigger>
              </TabsList>

              <div className="max-h-[500px] overflow-auto">
                <TabsContent value="message" className="m-0 p-4">
                  {message.error ? (
                    <div className="text-xs text-destructive bg-destructive/10 rounded-md p-3">
                      {message.error}
                    </div>
                  ) : message.message ? (
                    <div className="font-mono text-xs leading-relaxed">
                      <JsonTreeViewer data={message.message} defaultExpanded={3} />
                    </div>
                  ) : message.rawMessage ? (
                    <pre className="text-xs font-mono text-muted-foreground whitespace-pre-wrap break-all">
                      {message.rawMessage}
                    </pre>
                  ) : (
                    <span className="text-muted-foreground text-xs italic">
                      No message body
                    </span>
                  )}
                </TabsContent>

                <TabsContent value="headers" className="m-0 p-4">
                  {message.headers && Object.keys(message.headers).length > 0 ? (
                    <div className="space-y-1">
                      {Object.entries(message.headers).map(([key, val]) => (
                        <div
                          key={key}
                          className="flex items-start gap-3 text-xs font-mono py-1 border-b border-white/[0.04] last:border-0"
                        >
                          <span className="text-sky-300 shrink-0 min-w-[140px]">
                            {key}
                          </span>
                          <span className="text-muted-foreground break-all">
                            {val}
                          </span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <span className="text-muted-foreground text-xs italic">
                      No headers
                    </span>
                  )}
                </TabsContent>

                <TabsContent value="metadata" className="m-0 p-4">
                  <div className="grid grid-cols-2 gap-x-8 gap-y-2 text-xs">
                    {[
                      ["Topic", message.topic],
                      ["Partition", String(message.partition)],
                      ["Offset", formatOffset(message.offset)],
                      ["Timestamp", formatTimestamp(message.timestamp)],
                      ["Key", message.key ?? "—"],
                      ["Schema ID", message.schemaId != null ? String(message.schemaId) : "—"],
                      ["Schema Type", message.schemaType ?? "—"],
                      ["Schema Name", message.schemaName ?? "—"],
                    ].map(([label, value]) => (
                      <div key={label} className="flex flex-col gap-0.5">
                        <span className="text-muted-foreground/60 text-[10px] uppercase tracking-wider">
                          {label}
                        </span>
                        <span className="font-mono text-foreground/80 break-all">
                          {value}
                        </span>
                      </div>
                    ))}
                  </div>
                </TabsContent>

                <TabsContent value="raw" className="m-0 p-4">
                  <pre className="text-xs font-mono text-muted-foreground whitespace-pre-wrap break-all">
                    {message.rawMessage ?? "—"}
                  </pre>
                </TabsContent>
              </div>
            </Tabs>
          </div>
        </CollapsibleContent>
      </div>
    </Collapsible>
  );
}
