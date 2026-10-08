"use client";

import { useMemo, useState } from "react";

import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import { SessionStatusChip } from "@/features/sessions/components/SessionStatusChip";
import type { WalkSession } from "@/features/sessions/types";
import { formatDateTime, formatDuration } from "@/lib/format";

import { useAllSessions, useEndSession } from "../hooks/useAdminSessions";

export function SessionsTable() {
  const sessions = useAllSessions();
  const end = useEndSession();
  const [ending, setEnding] = useState<WalkSession | null>(null);

  const rows = useMemo(() => [...(sessions.data ?? [])].sort((a, b) => b.startTime.localeCompare(a.startTime)), [sessions.data]);

  const columns: readonly Column<WalkSession>[] = [
    { key: "id", header: "Session", cell: (s) => <span className="font-semibold">#{s.id}</span> },
    { key: "user", header: "User", cell: (s) => `#${s.userId}` },
    { key: "started", header: "Started", cell: (s) => <span className="text-muted-foreground">{formatDateTime(s.startTime)}</span> },
    { key: "duration", header: "Duration", cell: (s) => formatDuration(s.startTime, s.endTime) ?? "—" },
    { key: "status", header: "Status", cell: (s) => <SessionStatusChip status={s.status} /> },
    {
      key: "actions",
      header: "Actions",
      className: "text-right",
      cell: (s) =>
        s.status === "ACTIVE" ? (
          <Button type="button" variant="secondary" size="sm" onClick={() => setEnding(s)}>
            End session
          </Button>
        ) : null,
    },
  ];

  return (
    <>
      <DataTable
        columns={columns}
        rows={rows}
        getKey={(s) => s.id}
        isLoading={sessions.isPending}
        error={sessions.error}
        onRetry={() => void sessions.refetch()}
        emptyTitle="No walk sessions yet"
        emptyDescription="Sessions appear here once walkers start them from the mobile app."
      />
      <ConfirmDialog
        open={ending !== null}
        onOpenChange={(open) => !open && setEnding(null)}
        title="End this session?"
        description="The walker's session is closed immediately."
        confirmLabel="End session"
        isPending={end.isPending}
        onConfirm={() => ending && end.mutate(ending.id, { onSuccess: () => setEnding(null) })}
      />
    </>
  );
}
