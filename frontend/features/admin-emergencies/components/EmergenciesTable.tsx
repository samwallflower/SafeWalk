"use client";

import { MoreHorizontalIcon } from "lucide-react";
import { useMemo, useState } from "react";

import { Chip } from "@/components/shared/Chip";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { SOURCE_LABEL, TRIGGER_SOURCES } from "@/features/emergencies/lib/source-labels";
import type { Emergency, EmergencyTriggerSource } from "@/features/emergencies/types";
import { useStreetName } from "@/hooks/useStreetName";
import { formatDateTime } from "@/lib/format";

import { useAllEmergencies, useDeleteEmergency, useSetEmergencyResolved } from "../hooks/useAdminEmergencies";

function Location({ emergency }: { emergency: Emergency }) {
  const street = useStreetName(emergency.triggerLatitude, emergency.triggerLongitude);
  return <span>{street.data ?? "—"}</span>;
}

const ALL = "all";

function isSource(value: string): value is EmergencyTriggerSource {
  return (TRIGGER_SOURCES as readonly string[]).includes(value);
}

export function EmergenciesTable() {
  const emergencies = useAllEmergencies();
  const setResolved = useSetEmergencyResolved();
  const remove = useDeleteEmergency();
  const [source, setSource] = useState<EmergencyTriggerSource | typeof ALL>(ALL);
  const [deleting, setDeleting] = useState<Emergency | null>(null);

  const rows = useMemo(() => {
    const sorted = [...(emergencies.data ?? [])].sort((a, b) => b.triggerTimestamp.localeCompare(a.triggerTimestamp));
    return source === ALL ? sorted : sorted.filter((e) => e.triggerSource === source);
  }, [emergencies.data, source]);

  const items = [{ value: ALL, label: "All trigger sources" }, ...TRIGGER_SOURCES.map((s) => ({ value: s, label: SOURCE_LABEL[s] }))];

  const columns: readonly Column<Emergency>[] = [
    { key: "id", header: "ID", cell: (e) => <span className="font-semibold">#{e.id}</span> },
    { key: "source", header: "Trigger", cell: (e) => SOURCE_LABEL[e.triggerSource] },
    { key: "when", header: "When", cell: (e) => <span className="text-muted-foreground">{formatDateTime(e.triggerTimestamp)}</span> },
    { key: "where", header: "Location", cell: (e) => <Location emergency={e} /> },
    { key: "session", header: "Session", cell: (e) => (e.walkSession ? `#${e.walkSession.id}` : "—") },
    {
      key: "status",
      header: "Status",
      cell: (e) => (
        <Chip tone={e.resolved ? "success" : "danger"}>{e.resolved ? "Resolved" : "Unresolved"}</Chip>
      ),
    },
    {
      key: "actions",
      header: "Actions",
      className: "text-right",
      cell: (e) => (
        <DropdownMenu>
          <DropdownMenuTrigger render={<Button variant="ghost" size="icon-sm" aria-label={`Actions for emergency ${e.id}`} />}>
            <MoreHorizontalIcon />
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem disabled={setResolved.isPending} onClick={() => setResolved.mutate({ id: e.id, resolved: !e.resolved })}>
              {e.resolved ? "Mark unresolved" : "Mark resolved"}
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setDeleting(e)}>Delete</DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Select
          items={items}
          value={source}
          onValueChange={(v) => {
            if (v === ALL) setSource(ALL);
            else if (v !== null && isSource(v)) setSource(v);
          }}
        >
          <SelectTrigger className="w-60 border-transparent bg-muted" aria-label="Filter by trigger source">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {items.map((item) => (
              <SelectItem key={item.value} value={item.value}>
                {item.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <DataTable
        columns={columns}
        rows={rows}
        getKey={(e) => e.id}
        isLoading={emergencies.isPending}
        error={emergencies.error}
        onRetry={() => void emergencies.refetch()}
        emptyTitle={source === ALL ? "No emergencies recorded" : "No emergencies from this source"}
      />
      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Delete this emergency?"
        description="The record is removed, and its walk session may return to active. Use this only for false records."
        confirmLabel="Delete"
        isPending={remove.isPending}
        onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })}
      />
    </div>
  );
}
