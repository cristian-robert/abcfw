"use client";

import { Inbox } from "lucide-react";

export function EmptyState() {
  return (
    <div className="flex-1 flex flex-col items-center justify-center gap-6 text-muted-foreground/40">
      <div className="rounded-2xl bg-white/[0.03] p-6 border border-white/[0.06]">
        <Inbox className="h-10 w-10 stroke-[1.2] text-muted-foreground/30" />
      </div>
      <div className="text-center">
        <p className="text-sm font-medium text-muted-foreground/60">
          No topic selected
        </p>
        <p className="text-xs mt-1.5 text-muted-foreground/40">
          Choose a topic from the sidebar to browse messages
        </p>
      </div>
    </div>
  );
}
