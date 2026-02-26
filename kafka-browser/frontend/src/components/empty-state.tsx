"use client";

import { Layers, ArrowLeft } from "lucide-react";

export function EmptyState() {
  return (
    <div className="flex-1 flex flex-col items-center justify-center gap-5 text-muted-foreground/60">
      <div className="relative">
        <div className="animate-float">
          <div className="relative">
            <Layers className="h-16 w-16 stroke-[0.8] text-muted-foreground/20" />
            <div className="absolute inset-0 blur-xl bg-teal-500/[0.04] rounded-full" />
          </div>
        </div>
        <ArrowLeft className="absolute -bottom-1 -left-5 h-5 w-5 text-teal-400/30 animate-[float_3s_ease-in-out_infinite_0.5s]" />
      </div>
      <div className="text-center space-y-1.5">
        <p className="text-sm font-medium text-muted-foreground">
          No topic selected
        </p>
        <p className="text-xs text-muted-foreground/50">
          Choose a topic from the sidebar to browse messages
        </p>
      </div>
    </div>
  );
}
