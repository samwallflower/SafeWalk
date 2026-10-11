"use client";

import { EyeIcon, FlagIcon, MoreVerticalIcon, SquareIcon, Trash2Icon } from "lucide-react";
import { useMemo, useState } from "react";

import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { SessionStatusChip } from "@/features/sessions/components/SessionStatusChip";
import type { SessionStatus, WalkSession } from "@/features/sessions/types";
import { formatDateTime, formatDuration } from "@/lib/format";

import { useAllSessions, useDeleteSession, useEndSession, useSetSessionStatus } from "../hooks/useAdminSessions";
import { SessionDrawer } from "./SessionDrawer";

type Settable = Exclude<SessionStatus, "COMPLETED">;
const STATUS_ACTIONS: readonly { status: Settable; label: string }[] = [
  { status: "ACTIVE", label: "Mark as active" },
  { status: "EMERGENCY", label: "Mark as emergency" },
  { status: "ABANDONED", label: "Mark as abandoned" },
];

export function SessionsTable() {
  const sessions = useAllSessions();
  const end = useEndSession();
  const setStatus = useSetSessionStatus();
  const remove = useDeleteSession();
  const [viewing, setViewing] = useState<WalkSession | null>(null);
  const [ending, setEnding] = useState<WalkSession | null>(null);
  const [deleting, setDeleting] = useState<WalkSession | null>(null);

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
      cell: (s) => (
        <span className="flex justify-end gap-1">
          <Button type="button" variant="ghost" size="icon-sm" aria-label={`View session ${s.id}`} onClick={() => setViewing(s)}>
            <EyeIcon />
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger render={<Button variant="ghost" size="icon-sm" aria-label={`More actions for session ${s.id}`} />}>
              <MoreVerticalIcon />
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {s.status === "ACTIVE" ? (
                <DropdownMenuItem onClick={() => setEnding(s)}>
                  <SquareIcon /> End session
                </DropdownMenuItem>
              ) : null}
              {s.status !== "COMPLETED"
                ? STATUS_ACTIONS.filter((a) => a.status !== s.status).map((a) => (
                    <DropdownMenuItem key={a.status} onClick={() => setStatus.mutate({ id: s.id, status: a.status })}>
                      <FlagIcon /> {a.label}
                    </DropdownMenuItem>
                  ))
                : null}
              {s.status !== "COMPLETED" ? <DropdownMenuSeparator /> : null}
              <DropdownMenuItem variant="destructive" onClick={() => setDeleting(s)}>
                <Trash2Icon /> Delete session
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </span>
      ),
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
      <SessionDrawer session={viewing} onOpenChange={(open) => !open && setViewing(null)} />
      <ConfirmDialog
        open={ending !== null}
        onOpenChange={(open) => !open && setEnding(null)}
        title="End this session?"
        description="The walker's session is closed immediately."
        confirmLabel="End session"
        isPending={end.isPending}
        onConfirm={() => ending && end.mutate(ending.id, { onSuccess: () => setEnding(null) })}
      />
      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Delete this session?"
        description={`Session #${deleting?.id ?? ""} is removed from the walker's history. This can't be undone.`}
        confirmLabel="Delete"
        isPending={remove.isPending}
        onConfirm={() => deleting && remove.mutate({ id: deleting.id, userId: deleting.userId }, { onSettled: () => setDeleting(null) })}
      />
    </>
  );
}
