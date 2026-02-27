"use client";

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

interface ConfirmSendDialogProps {
  open: boolean;
  topic: string;
  schemaSubject: string | null;
  messageCount: number;
  onConfirm: () => void;
  onCancel: () => void;
  sending?: boolean;
}

export function ConfirmSendDialog({
  open,
  topic,
  schemaSubject,
  messageCount,
  onConfirm,
  onCancel,
  sending = false,
}: ConfirmSendDialogProps) {
  return (
    <Dialog open={open} onOpenChange={(isOpen) => !isOpen && onCancel()}>
      <DialogContent className="sm:max-w-[400px]">
        <DialogHeader>
          <DialogTitle>Confirm Send</DialogTitle>
          <DialogDescription>
            You are about to produce messages to Kafka.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-2 py-4 text-sm">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Topic</span>
            <span className="font-mono">{topic}</span>
          </div>
          {schemaSubject && (
            <div className="flex justify-between">
              <span className="text-muted-foreground">Schema</span>
              <span className="font-mono">{schemaSubject}</span>
            </div>
          )}
          <div className="flex justify-between">
            <span className="text-muted-foreground">Messages</span>
            <span>
              {messageCount} {messageCount === 1 ? "message" : "messages"}
            </span>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onCancel} disabled={sending}>
            Cancel
          </Button>
          <Button onClick={onConfirm} disabled={sending}>
            {sending ? "Sending..." : "Confirm Send"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
