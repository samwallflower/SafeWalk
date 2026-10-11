"use client";

import { useMemo } from "react";

import { ColumnChart } from "@/components/shared/ColumnChart";
import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { ListSkeleton } from "@/components/shared/ListSkeleton";
import { SectionPanel } from "@/components/shared/SectionPanel";
import { useMySessions } from "@/features/sessions/hooks/useMySessions";

import { walksPerDay } from "../lib/user-stats";

const DAYS = 7;

export function WalksChartPanel() {
  const sessions = useMySessions();
  const days = useMemo(() => (sessions.data ? walksPerDay(sessions.data, DAYS) : []), [sessions.data]);
  const total = days.reduce((s, d) => s + d.value, 0);
  return (
    <SectionPanel title="Walks, last 7 days" headerExtra={sessions.data ? <span className="text-sm text-muted-foreground">{total} {total === 1 ? "walk" : "walks"}</span> : null}>
      {sessions.isPending ? (
        <ListSkeleton rows={3} />
      ) : sessions.isError ? (
        <ErrorState message={sessions.error.message} onRetry={() => void sessions.refetch()} />
      ) : total === 0 ? (
        <EmptyState title="No walks this week" description="Start a walk in the mobile app and it will show up here." />
      ) : (
        <ColumnChart items={days} ariaLabel={`Walks per day for the last ${DAYS} days, ${total} in total`} />
      )}
    </SectionPanel>
  );
}
