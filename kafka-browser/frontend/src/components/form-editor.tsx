"use client";

import { useState, useMemo } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { FormFieldRenderer } from "@/components/form-field-renderer";
import { Save, Send } from "lucide-react";

interface FormEditorProps {
  name: string;
  jsonContent: string;
  onNameChange: (name: string) => void;
  onJsonChange: (json: string) => void;
  onSave: () => void;
  onSend: () => void;
  saving: boolean;
  sendDisabled: boolean;
  sendResult?: { success: boolean; message: string } | null;
}

export function FormEditor({
  name,
  jsonContent,
  onNameChange,
  onJsonChange,
  onSave,
  onSend,
  saving,
  sendDisabled,
  sendResult,
}: FormEditorProps) {
  const [viewMode, setViewMode] = useState<"form" | "raw">("form");
  const [rawContent, setRawContent] = useState(jsonContent);
  const [rawParseError, setRawParseError] = useState<string | null>(null);

  const { parsedData, parseError } = useMemo(() => {
    try {
      return { parsedData: JSON.parse(jsonContent), parseError: null };
    } catch (e) {
      return { parsedData: null, parseError: e instanceof Error ? e.message : "Invalid JSON" };
    }
  }, [jsonContent]);

  const handleFormChange = (value: unknown) => {
    const json = JSON.stringify(value, null, 2);
    onJsonChange(json);
    setRawContent(json);
  };

  const handleRawChange = (value: string) => {
    setRawContent(value);
    try {
      JSON.parse(value);
      onJsonChange(value);
      setRawParseError(null);
    } catch (e) {
      setRawParseError(e instanceof Error ? e.message : "Invalid JSON");
    }
  };

  const handleModeSwitch = (mode: "form" | "raw") => {
    if (mode === "raw") {
      setRawContent(jsonContent);
    } else if (mode === "form") {
      try {
        JSON.parse(rawContent);
        onJsonChange(rawContent);
      } catch {
        return;
      }
    }
    setViewMode(mode);
  };

  return (
    <div className="flex flex-col h-full flex-1 min-h-0">
      <div className="flex items-center gap-2 px-4 h-10 border-b border-white/[0.06] shrink-0">
        <Input
          value={name}
          onChange={(e) => onNameChange(e.target.value)}
          placeholder="Template name"
          className="h-7 text-xs w-48"
        />
        <div className="flex-1" />
        <div className="flex items-center gap-1 text-xs text-muted-foreground">
          <button
            className={
              viewMode === "form"
                ? "text-teal-400 font-medium"
                : "hover:text-foreground"
            }
            onClick={() => handleModeSwitch("form")}
          >
            Form
          </button>
          <span className="mx-1">|</span>
          <button
            className={
              viewMode === "raw"
                ? "text-teal-400 font-medium"
                : "hover:text-foreground"
            }
            onClick={() => handleModeSwitch("raw")}
          >
            Raw JSON
          </button>
        </div>
      </div>

      <ScrollArea className="flex-1 min-h-0">
        <div className="p-4">
          {viewMode === "form" && parsedData !== null && (
            <div className="space-y-0.5">
              {typeof parsedData === "object" && !Array.isArray(parsedData) ? (
                Object.keys(parsedData).map((key) => (
                  <FormFieldRenderer
                    key={key}
                    label={key}
                    value={parsedData[key]}
                    depth={0}
                    onChange={(v) =>
                      handleFormChange({ ...parsedData, [key]: v })
                    }
                    onDelete={() => {
                      const next = { ...parsedData };
                      delete next[key];
                      handleFormChange(next);
                    }}
                  />
                ))
              ) : (
                <FormFieldRenderer
                  label="root"
                  value={parsedData}
                  depth={0}
                  onChange={handleFormChange}
                />
              )}
              <Button
                variant="ghost"
                size="sm"
                className="h-7 text-xs text-muted-foreground mt-2"
                onClick={() => {
                  if (
                    typeof parsedData === "object" &&
                    !Array.isArray(parsedData)
                  ) {
                    const key = `newField${Object.keys(parsedData).length}`;
                    handleFormChange({ ...parsedData, [key]: "" });
                  }
                }}
              >
                + Add Field
              </Button>
            </div>
          )}

          {viewMode === "form" && parsedData === null && (
            <div className="text-xs text-red-400 py-4">
              Cannot render form: {parseError}
            </div>
          )}

          {viewMode === "raw" && (
            <textarea
              value={rawContent}
              onChange={(e) => handleRawChange(e.target.value)}
              className="w-full h-[500px] bg-transparent text-xs font-mono resize-none focus:outline-none text-foreground"
              spellCheck={false}
            />
          )}

          {rawParseError && viewMode === "raw" && (
            <div className="text-[10px] text-red-400 mt-1">{rawParseError}</div>
          )}
        </div>
      </ScrollArea>

      <div className="flex items-center gap-2 px-4 h-12 border-t border-white/[0.06] shrink-0">
        <Button
          size="sm"
          variant="outline"
          onClick={onSave}
          disabled={saving || !name.trim()}
        >
          <Save className="h-3.5 w-3.5 mr-1.5" />
          {saving ? "Saving..." : "Save"}
        </Button>
        <Button size="sm" onClick={onSend} disabled={sendDisabled}>
          <Send className="h-3.5 w-3.5 mr-1.5" />
          Send to Kafka
        </Button>
        {sendResult && (
          <span
            className={`text-xs ${sendResult.success ? "text-emerald-400" : "text-red-400"}`}
          >
            {sendResult.message}
          </span>
        )}
      </div>
    </div>
  );
}
