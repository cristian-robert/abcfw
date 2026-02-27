"use client";

import { useState, useCallback } from "react";
import { TemplateList } from "@/components/template-list";
import { TemplateEditor } from "@/components/template-editor";
import { BulkSendDialog } from "@/components/bulk-send-dialog";
import { useTemplates } from "@/hooks/use-templates";
import { useProducer } from "@/hooks/use-producer";
import { useSchemas } from "@/hooks/use-schemas";
import { FileJson } from "lucide-react";

interface BulkFile {
  name: string;
  content: string;
}

export default function TemplatesPage() {
  const { templates, loading, get, create, update, remove } = useTemplates();
  const { send, sendBulk } = useProducer();
  const { subjects } = useSchemas();

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [name, setName] = useState("");
  const [topicName, setTopicName] = useState("");
  const [schemaSubject, setSchemaSubject] = useState("");
  const [jsonContent, setJsonContent] = useState("");
  const [saving, setSaving] = useState(false);
  const [sending, setSending] = useState(false);
  const [sendResult, setSendResult] = useState<{
    success: boolean;
    message: string;
  } | null>(null);
  const [bulkFiles, setBulkFiles] = useState<BulkFile[] | null>(null);

  const loadTemplate = useCallback(
    async (id: number) => {
      const template = await get(id);
      if (template) {
        setSelectedId(template.id);
        setName(template.name);
        setTopicName(template.topicName || "");
        setSchemaSubject(template.schemaSubject || "");
        setJsonContent(template.jsonContent || "{}");
        setSendResult(null);
      }
    },
    [get]
  );

  const handleUpload = useCallback(
    async (fileName: string, content: string) => {
      try {
        JSON.parse(content);
      } catch {
        return;
      }

      const result = await create({
        name: fileName,
        topicName: "",
        schemaSubject: "",
        jsonContent: content,
      });

      if (result) {
        setSelectedId(result.id);
        setName(result.name);
        setTopicName(result.topicName || "");
        setSchemaSubject(result.schemaSubject || "");
        setJsonContent(result.jsonContent || "{}");
        setSendResult(null);
      }
    },
    [create]
  );

  const handleBulkUpload = useCallback((files: BulkFile[]) => {
    setBulkFiles(files);
  }, []);

  const handleDelete = useCallback(
    async (id: number) => {
      await remove(id);
      if (selectedId === id) {
        setSelectedId(null);
        setName("");
        setTopicName("");
        setSchemaSubject("");
        setJsonContent("");
        setSendResult(null);
      }
    },
    [remove, selectedId]
  );

  const handleSave = useCallback(async () => {
    setSaving(true);
    const data = { name, topicName, schemaSubject, jsonContent };
    if (selectedId) {
      await update(selectedId, data);
    } else {
      const result = await create(data);
      if (result) setSelectedId(result.id);
    }
    setSaving(false);
  }, [selectedId, name, topicName, schemaSubject, jsonContent, create, update]);

  const handleSend = useCallback(async () => {
    if (!topicName.trim()) return;
    setSending(true);
    setSendResult(null);
    const result = await send(
      topicName,
      jsonContent,
      schemaSubject || undefined
    );
    if (result) {
      setSendResult(
        result.success
          ? { success: true, message: `Sent to partition ${result.partition}, offset ${result.offset}` }
          : { success: false, message: result.error || "Failed to send" }
      );
    }
    setSending(false);
  }, [topicName, jsonContent, schemaSubject, send]);

  const hasContent = jsonContent && jsonContent !== "{}";

  return (
    <div className="flex h-full overflow-hidden">
      <TemplateList
        templates={templates}
        loading={loading}
        selectedId={selectedId}
        onSelect={loadTemplate}
        onUpload={handleUpload}
        onBulkUpload={handleBulkUpload}
        onDelete={handleDelete}
      />
      <div className="flex-1 overflow-hidden">
        {bulkFiles ? (
          <BulkSendDialog
            files={bulkFiles}
            subjects={subjects}
            onSend={sendBulk}
            onClose={() => setBulkFiles(null)}
          />
        ) : hasContent ? (
          <TemplateEditor
            name={name}
            topicName={topicName}
            schemaSubject={schemaSubject}
            jsonContent={jsonContent}
            subjects={subjects}
            onNameChange={setName}
            onTopicChange={setTopicName}
            onSchemaSubjectChange={setSchemaSubject}
            onJsonChange={setJsonContent}
            onSave={handleSave}
            onSend={handleSend}
            saving={saving}
            sending={sending}
            sendResult={sendResult}
          />
        ) : (
          <div className="flex flex-col items-center justify-center h-full text-muted-foreground">
            <FileJson className="h-16 w-16 mb-4 opacity-30" />
            <p className="text-lg">No template selected</p>
            <p className="text-sm mt-1">
              Select a template from the list, or upload a JSON file
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
