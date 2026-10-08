"use client";

import { useMemo } from "react";

import { BarList } from "@/components/shared/BarList";
import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { ListSkeleton } from "@/components/shared/ListSkeleton";
import { SectionPanel } from "@/components/shared/SectionPanel";
import { useAllSessions } from "@/features/admin-sessions/hooks/useAdminSessions";

import { countSessionsByStatus } from "../lib/admin-stats";

const SESSION_COLORS = {
  COMPLETED: "#0a6b32",
  ACTIVE: "#0b52b8",
  EMERGENCY: "#c4161c",
  ABANDONED: "#5b6475",
} as const;

export function SessionsByStatus() {
  const sessions = useAllSessions();
  const counts = useMemo(
    () => (sessions.data ? countSessionsByStatus(sessions.data) : null),
    [sessions.data],
  );
  return (
    <SectionPanel title="Walk sessions by status">
      {sessions.isPending ? (
        <ListSkeleton rows={4} />
      ) : sessions.isError ? (
        <ErrorState
          message={sessions.error.message}
          onRetry={() => void sessions.refetch()}
        />
      ) : !counts || sessions.data.length === 0 ? (
        <EmptyState title="No walk sessions yet" />
      ) : (
        <BarList
          items={(Object.keys(counts) as (keyof typeof counts)[]).map(
            (status) => ({
              label: status.charAt(0) + status.slice(1).toLowerCase(),
              value: counts[status],
              color: SESSION_COLORS[status],
            }),
          )}
        />
      )}
    </SectionPanel>
  );
}
