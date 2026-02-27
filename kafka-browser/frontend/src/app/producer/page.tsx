"use client";

import { useState, useCallback, useEffect, useMemo } from "react";
import { useCollections } from "@/hooks/use-collections";
import { useTemplates } from "@/hooks/use-templates";
import { useProducer } from "@/hooks/use-producer";
import { useSchemas } from "@/hooks/use-schemas";
import { useSettings } from "@/hooks/use-settings";
import { CollectionSidebar } from "@/components/collection-sidebar";
import { TemplateListPanel } from "@/components/template-list-panel";
import { FormEditor } from "@/components/form-editor";
import { ConfirmSendDialog } from "@/components/confirm-send-dialog";
import { GlobalDefaultsBar } from "@/components/global-defaults-bar";
import { CollectionDetail } from "@/lib/types";
import { fetchCollection, updateCollection } from "@/lib/api";

export default function ProducerPage() {
  const collections = useCollections();
  const [selectedCollectionId, setSelectedCollectionId] = useState<number | null>(null);
  const [collectionDetail, setCollectionDetail] = useState<CollectionDetail | null>(null);

  const templates = useTemplates(selectedCollectionId);
  const [selectedTemplateId, setSelectedTemplateId] = useState<number | null>(null);

  const [name, setName] = useState("");
  const [jsonContent, setJsonContent] = useState("");
  const [saving, setSaving] = useState(false);
  const [sendResult, setSendResult] = useState<{ success: boolean; message: string } | null>(null);

  const producer = useProducer();
  const schemas = useSchemas();
  const appSettings = useSettings();

  const effectiveTopic = useMemo(
    () => collectionDetail?.topicName || appSettings.settings["global.topic"] || "",
    [collectionDetail?.topicName, appSettings.settings]
  );
  const effectiveSchema = useMemo(
    () => collectionDetail?.schemaSubject || appSettings.settings["global.schemaSubject"] || "",
    [collectionDetail?.schemaSubject, appSettings.settings]
  );

  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    topic: string;
    schema: string | null;
    count: number;
    mode: "single" | "collection";
  }>({ open: false, topic: "", schema: null, count: 0, mode: "single" });

  useEffect(() => {
    if (selectedCollectionId) {
      fetchCollection(selectedCollectionId).then(setCollectionDetail).catch(() => setCollectionDetail(null));
    } else {
      setCollectionDetail(null);
    }
    setSelectedTemplateId(null);
    setName("");
    setJsonContent("");
    setSendResult(null);
  }, [selectedCollectionId]);

  const handleSelectTemplate = useCallback(async (id: number) => {
    setSelectedTemplateId(id);
    setSendResult(null);
    const detail = await templates.get(id);
    if (detail) {
      setName(detail.name);
      setJsonContent(detail.jsonContent || "{}");
    }
  }, [templates]);

  const handleCreateCollection = useCallback(async (collName: string) => {
    const result = await collections.create({
      name: collName,
      topicName: null,
      schemaSubject: null,
      avscContent: null,
    });
    if (result) setSelectedCollectionId(result.id);
  }, [collections]);

  const handleUploadAvsc = useCallback(async (collId: number, content: string) => {
    const detail = await collections.get(collId);
    if (detail) {
      await collections.update(collId, {
        name: detail.name,
        topicName: detail.topicName,
        schemaSubject: detail.schemaSubject,
        avscContent: content,
      });
      if (collId === selectedCollectionId) {
        const updated = await fetchCollection(collId);
        setCollectionDetail(updated);
      }
    }
  }, [collections, selectedCollectionId]);

  const handleUploadTemplate = useCallback(async (fileName: string, content: string) => {
    await templates.create({
      name: fileName,
      jsonContent: content,
      collectionId: selectedCollectionId,
    });
  }, [templates, selectedCollectionId]);

  const handleSave = useCallback(async () => {
    if (!name.trim()) return;
    setSaving(true);
    try {
      if (selectedTemplateId) {
        await templates.update(selectedTemplateId, {
          name,
          jsonContent,
          collectionId: selectedCollectionId,
        });
      } else {
        const result = await templates.create({
          name,
          jsonContent,
          collectionId: selectedCollectionId,
        });
        if (result) setSelectedTemplateId(result.id);
      }
      setSendResult({ success: true, message: "Saved" });
    } catch {
      setSendResult({ success: false, message: "Failed to save" });
    } finally {
      setSaving(false);
    }
  }, [name, jsonContent, selectedTemplateId, selectedCollectionId, templates]);

  const handleSendSingle = useCallback(() => {
    if (!effectiveTopic) return;
    setConfirmDialog({
      open: true,
      topic: effectiveTopic,
      schema: effectiveSchema || null,
      count: 1,
      mode: "single",
    });
  }, [effectiveTopic, effectiveSchema]);

  const handleRunAll = useCallback(() => {
    if (!effectiveTopic) return;
    setConfirmDialog({
      open: true,
      topic: effectiveTopic,
      schema: effectiveSchema || null,
      count: templates.templates.length,
      mode: "collection",
    });
  }, [effectiveTopic, effectiveSchema, templates.templates.length]);

  const handleConfirmSend = useCallback(async () => {
    if (confirmDialog.mode === "single") {
      const result = await producer.send(
        confirmDialog.topic,
        jsonContent,
        confirmDialog.schema || undefined,
      );
      setSendResult(
        result
          ? { success: result.success, message: result.success ? `Sent to partition ${result.partition} offset ${result.offset}` : result.error || "Failed" }
          : { success: false, message: "Failed to send" }
      );
    } else if (confirmDialog.mode === "collection" && selectedCollectionId) {
      const result = await collections.run(selectedCollectionId);
      setSendResult(
        result
          ? { success: result.failed === 0, message: `${result.succeeded}/${result.total} sent` }
          : { success: false, message: "Failed to run collection" }
      );
    }
    setConfirmDialog((prev) => ({ ...prev, open: false }));
  }, [confirmDialog, jsonContent, producer, collections, selectedCollectionId]);

  const handleTopicChange = useCallback(async (topic: string) => {
    if (!collectionDetail || !selectedCollectionId) return;
    const updated = await updateCollection(selectedCollectionId, {
      name: collectionDetail.name,
      topicName: topic,
      schemaSubject: collectionDetail.schemaSubject,
      avscContent: collectionDetail.avscContent,
    });
    setCollectionDetail(updated);
    collections.refresh();
  }, [collectionDetail, selectedCollectionId, collections]);

  const handleSchemaChange = useCallback(async (schema: string) => {
    if (!collectionDetail || !selectedCollectionId) return;
    const updated = await updateCollection(selectedCollectionId, {
      name: collectionDetail.name,
      topicName: collectionDetail.topicName,
      schemaSubject: schema,
      avscContent: collectionDetail.avscContent,
    });
    setCollectionDetail(updated);
    collections.refresh();
  }, [collectionDetail, selectedCollectionId, collections]);

  const hasContent = selectedTemplateId !== null || jsonContent.length > 2;

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <GlobalDefaultsBar
        globalTopic={appSettings.settings["global.topic"] || ""}
        globalSchema={appSettings.settings["global.schemaSubject"] || ""}
        subjects={schemas.subjects}
        onTopicChange={(v) => appSettings.set("global.topic", v)}
        onSchemaChange={(v) => appSettings.set("global.schemaSubject", v)}
      />
      <div className="flex flex-1 min-h-0 overflow-hidden">
      <CollectionSidebar
        collections={collections.collections}
        loading={collections.loading}
        selectedId={selectedCollectionId}
        onSelect={setSelectedCollectionId}
        onCreate={handleCreateCollection}
        onDelete={async (id) => {
          await collections.remove(id);
          if (id === selectedCollectionId) {
            setSelectedCollectionId(null);
          }
        }}
        onUploadAvsc={handleUploadAvsc}
        onExport={(id) => collections.doExport(id)}
        onImport={async (data) => {
          const result = await collections.doImport(data);
          if (result) setSelectedCollectionId(result.id);
        }}
      />

      {selectedCollectionId && (
        <TemplateListPanel
          templates={templates.templates}
          loading={templates.loading}
          selectedId={selectedTemplateId}
          collection={collectionDetail}
          subjects={schemas.subjects}
          effectiveTopic={effectiveTopic}
          effectiveSchema={effectiveSchema}
          onSelect={handleSelectTemplate}
          onUpload={handleUploadTemplate}
          onDelete={async (id) => {
            await templates.remove(id);
            if (id === selectedTemplateId) {
              setSelectedTemplateId(null);
              setName("");
              setJsonContent("");
            }
          }}
          onDuplicate={async (id) => {
            const result = await templates.duplicate(id);
            if (result) {
              setSelectedTemplateId(result.id);
              setName(result.name);
              setJsonContent(result.jsonContent || "{}");
            }
          }}
          onRunAll={handleRunAll}
          onTopicChange={handleTopicChange}
          onSchemaChange={handleSchemaChange}
        />
      )}

      {selectedCollectionId && hasContent ? (
        <FormEditor
          name={name}
          jsonContent={jsonContent}
          onNameChange={setName}
          onJsonChange={setJsonContent}
          onSave={handleSave}
          onSend={handleSendSingle}
          saving={saving}
          sendDisabled={!effectiveTopic || !jsonContent.trim()}
          sendResult={sendResult}
        />
      ) : (
        <div className="flex-1 flex items-center justify-center text-sm text-muted-foreground/40">
          {selectedCollectionId
            ? "Select a template or upload a JSON file"
            : "Select a collection to get started"}
        </div>
      )}

      <ConfirmSendDialog
        open={confirmDialog.open}
        topic={confirmDialog.topic}
        schemaSubject={confirmDialog.schema}
        messageCount={confirmDialog.count}
        onConfirm={handleConfirmSend}
        onCancel={() => setConfirmDialog((prev) => ({ ...prev, open: false }))}
        sending={producer.sending}
      />
      </div>
    </div>
  );
}
