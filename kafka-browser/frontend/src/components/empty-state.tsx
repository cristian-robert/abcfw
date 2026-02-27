"use client";

import { Layers, ArrowLeft, MessageSquare } from "lucide-react";

export function EmptyState() {
  return (
    <div className="flex-1 flex flex-col items-center justify-center gap-6 text-muted-foreground/60">
      <div className="relative" aria-hidden="true">
        {/* Main floating icon */}
        <div className="animate-float">
          <div className="relative">
            <div className="absolute inset-0 blur-2xl bg-teal-500/[0.06] rounded-full scale-150" />
            <Layers className="h-16 w-16 stroke-[0.8] text-muted-foreground/15 relative" />
          </div>
        </div>

        {/* Arrow hint */}
        <ArrowLeft className="absolute -bottom-1 -left-6 h-5 w-5 text-teal-400/25 animate-[float_3s_ease-in-out_infinite_0.5s]" />

        {/* Small message icon */}
        <MessageSquare className="absolute -top-2 -right-4 h-4 w-4 text-teal-400/15 animate-[float_3s_ease-in-out_infinite_1s]" />
      </div>

      <div className="text-center space-y-2">
        <p className="text-sm font-medium text-muted-foreground/70">
          No topic selected
        </p>
        <p className="text-xs text-muted-foreground/35 max-w-[220px] leading-relaxed">
          Choose a topic from the sidebar to browse and inspect messages
        </p>
      </div>
    </div>
  );
}
