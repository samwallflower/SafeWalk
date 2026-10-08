"use client";

import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { ListSkeleton } from "@/components/shared/ListSkeleton";

import { useMySessions } from "../hooks/useMySessions";
import { SessionItem } from "./SessionItem";

interface SessionsListProps {
  limit?: number;
  expandable?: boolean;
}

export function SessionsList({ limit, expandable = false }: SessionsListProps) {
  const sessions = useMySessions();

  if (sessions.isPending) return <ListSkeleton rows={limit ?? 4} />;
  if (sessions.isError) return <ErrorState message={sessions.error.message} onRetry={() => void sessions.refetch()} />;
  if (sessions.data.length === 0) {
    return (
      <EmptyState
        title="No walks yet"
        description="Walk sessions are started from the SafeWalk mobile app and show up here afterwards."
      />
    );
  }
  const shown = limit ? sessions.data.slice(0, limit) : sessions.data;
  return (
    <ul className="space-y-3">
      {shown.map((session) => (
        <SessionItem key={session.id} session={session} expandable={expandable} />
      ))}
    </ul>
  );
}
