"use client";

import { Inbox } from "lucide-react";

export function EmptyState() {
  return (
    <div className="flex-1 flex flex-col items-center justify-center gap-4 text-muted-foreground/60">
      <Inbox className="h-16 w-16 stroke-[1]" />
      <div className="text-center">
        <p className="text-sm font-medium text-muted-foreground">
          No topic selected
        </p>
        <p className="text-xs mt-1">
          Choose a topic from the sidebar to browse messages
        </p>
      </div>
    </div>
  );
}
