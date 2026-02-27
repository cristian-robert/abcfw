"use client";

import { useState, useEffect } from "react";
import { Input } from "@/components/ui/input";
import { Globe } from "lucide-react";

interface GlobalDefaultsBarProps {
  globalTopic: string;
  globalSchema: string;
  subjects: string[];
  onTopicChange: (value: string) => void;
  onSchemaChange: (value: string) => void;
}

export function GlobalDefaultsBar({
  globalTopic,
  globalSchema,
  subjects,
  onTopicChange,
  onSchemaChange,
}: GlobalDefaultsBarProps) {
  const [localTopic, setLocalTopic] = useState(globalTopic);
  const [localSchema, setLocalSchema] = useState(globalSchema);

  useEffect(() => {
    setLocalTopic(globalTopic);
  }, [globalTopic]);

  useEffect(() => {
    setLocalSchema(globalSchema);
  }, [globalSchema]);

  return (
    <div className="flex items-center gap-3 px-4 h-10 border-b border-white/[0.06] shrink-0 bg-white/[0.02]">
      <Globe className="h-3.5 w-3.5 text-muted-foreground/60 shrink-0" />
      <span className="text-[10px] uppercase tracking-wider text-muted-foreground/60 shrink-0">
        Defaults
      </span>
      <div className="flex items-center gap-1.5">
        <label className="text-[10px] text-muted-foreground/60 shrink-0">Topic</label>
        <Input
          value={localTopic}
          onChange={(e) => setLocalTopic(e.target.value)}
          onBlur={() => {
            if (localTopic !== globalTopic) onTopicChange(localTopic);
          }}
          onKeyDown={(e) => {
            if (e.key === "Enter") (e.target as HTMLInputElement).blur();
          }}
          placeholder="global default topic"
          className="h-6 text-xs w-52"
        />
      </div>
      <div className="flex items-center gap-1.5">
        <label className="text-[10px] text-muted-foreground/60 shrink-0">Schema</label>
        <Input
          value={localSchema}
          onChange={(e) => setLocalSchema(e.target.value)}
          onBlur={() => {
            if (localSchema !== globalSchema) onSchemaChange(localSchema);
          }}
          onKeyDown={(e) => {
            if (e.key === "Enter") (e.target as HTMLInputElement).blur();
          }}
          placeholder="global default schema"
          list="global-schema-subjects"
          className="h-6 text-xs w-52"
        />
        <datalist id="global-schema-subjects">
          {subjects.map((s) => (
            <option key={s} value={s} />
          ))}
        </datalist>
      </div>
      <span className="text-[10px] text-muted-foreground/40 ml-2">
        Collections without a topic/schema will use these defaults
      </span>
    </div>
  );
}
