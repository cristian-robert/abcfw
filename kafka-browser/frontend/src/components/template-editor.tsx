"use client";

import { useState, useEffect, useCallback } from "react";
import { EditableJsonTree } from "@/components/editable-json-tree";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Save, Send, TreePine, Code, AlertCircle } from "lucide-react";

interface TemplateEditorProps {
  name: string;
  topicName: string;
  schemaSubject: string;
  jsonContent: string;
  subjects: string[];
  onNameChange: (name: string) => void;
  onTopicChange: (topic: string) => void;
  onSchemaSubjectChange: (subject: string) => void;
  onJsonChange: (json: string) => void;
  onSave: () => void;
  onSend: () => void;
  saving: boolean;
  sending: boolean;
  sendResult?: { success: boolean; message: string } | null;
}

export function TemplateEditor({
  name,
  topicName,
  schemaSubject,
  jsonContent,
  subjects,
  onNameChange,
  onTopicChange,
  onSchemaSubjectChange,
  onJsonChange,
  onSave,
  onSend,
  saving,
  sending,
  sendResult,
}: TemplateEditorProps) {
  const [viewMode, setViewMode] = useState<"tree" | "raw">("tree");
  const [parsedJson, setParsedJson] = useState<unknown>(null);
  const [parseError, setParseError] = useState<string | null>(null);
  const [rawContent, setRawContent] = useState(jsonContent);

  useEffect(() => {
    setRawContent(jsonContent);
    try {
      setParsedJson(JSON.parse(jsonContent));
      setParseError(null);
    } catch (e) {
      setParsedJson(null);
      setParseError(e instanceof Error ? e.message : "Invalid JSON");
    }
  }, [jsonContent]);

  const handleTreeChange = useCallback(
    (newData: unknown) => {
      setParsedJson(newData);
      const newJson = JSON.stringify(newData, null, 2);
      setRawContent(newJson);
      onJsonChange(newJson);
    },
    [onJsonChange]
  );

  const handleRawChange = useCallback(
    (newRaw: string) => {
      setRawContent(newRaw);
      try {
        const parsed = JSON.parse(newRaw);
        setParsedJson(parsed);
        setParseError(null);
        onJsonChange(newRaw);
      } catch (e) {
        setParseError(e instanceof Error ? e.message : "Invalid JSON");
      }
    },
    [onJsonChange]
  );

  return (
    <div className="flex flex-col h-full">
      {/* Top controls */}
      <div className="p-3 border-b border-white/[0.06] space-y-2">
        <div className="flex gap-2">
          <Input
            value={name}
            onChange={(e) => onNameChange(e.target.value)}
            placeholder="Template name"
            className="h-8 text-sm"
          />
        </div>
        <div className="flex gap-2">
          <Input
            value={topicName}
            onChange={(e) => onTopicChange(e.target.value)}
            placeholder="Target topic"
            className="h-8 text-sm flex-1 font-mono"
          />
          <Input
            value={schemaSubject}
            onChange={(e) => onSchemaSubjectChange(e.target.value)}
            placeholder="Schema subject (optional)"
            className="h-8 text-sm flex-1 font-mono"
            list="schema-subjects"
          />
          <datalist id="schema-subjects">
            {subjects.map((s) => (
              <option key={s} value={s} />
            ))}
          </datalist>
        </div>
      </div>

      {/* View mode toggle */}
      <div className="flex items-center gap-1 px-3 py-2 border-b border-white/[0.06]">
        <Button
          variant={viewMode === "tree" ? "default" : "ghost"}
          size="xs"
          onClick={() => setViewMode("tree")}
          className="h-6 text-xs"
        >
          <TreePine className="h-3 w-3 mr-1" />
          Tree
        </Button>
        <Button
          variant={viewMode === "raw" ? "default" : "ghost"}
          size="xs"
          onClick={() => setViewMode("raw")}
          className="h-6 text-xs"
        >
          <Code className="h-3 w-3 mr-1" />
          Raw JSON
        </Button>
        {parseError && viewMode === "raw" && (
          <span className="ml-2 text-xs text-red-400 flex items-center gap-1">
            <AlertCircle className="h-3 w-3" />
            {parseError}
          </span>
        )}
      </div>

      {/* Editor area */}
      <ScrollArea className="flex-1">
        <div className="p-3">
          {viewMode === "tree" ? (
            parsedJson !== null ? (
              <EditableJsonTree
                data={parsedJson}
                onChange={handleTreeChange}
                defaultExpanded={3}
              />
            ) : (
              <div className="text-red-400 text-sm">
                <AlertCircle className="h-4 w-4 inline mr-1" />
                Cannot render tree: {parseError}
              </div>
            )
          ) : (
            <textarea
              value={rawContent}
              onChange={(e) => handleRawChange(e.target.value)}
              className="w-full min-h-[400px] bg-transparent text-sm font-mono text-foreground resize-none focus:outline-none focus:ring-1 focus:ring-teal-400/50 rounded-md p-2 border border-white/[0.06]"
              spellCheck={false}
            />
          )}
        </div>
      </ScrollArea>

      {/* Bottom actions */}
      <div className="p-3 border-t border-white/[0.06] flex items-center gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={onSave}
          disabled={saving || !name.trim()}
        >
          <Save className="h-4 w-4 mr-1" />
          {saving ? "Saving..." : "Save"}
        </Button>
        <Button
          size="sm"
          onClick={onSend}
          disabled={sending || !topicName.trim() || parseError !== null}
          className="bg-teal-600 hover:bg-teal-700 text-white"
        >
          <Send className="h-4 w-4 mr-1" />
          {sending ? "Sending..." : "Send to Kafka"}
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
