"use client";

import { useMemo } from "react";

import { BarList } from "@/components/shared/BarList";
import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { ListSkeleton } from "@/components/shared/ListSkeleton";
import { SectionPanel } from "@/components/shared/SectionPanel";

import { useWindowReports } from "../hooks/useWindowReports";
import { countByCategory, type StatsWindow } from "../lib/admin-stats";

export function IncidentBreakdown({ window }: { window: StatsWindow }) {
  const reports = useWindowReports(window);
  const items = useMemo(
    () => (reports.data ? countByCategory(reports.data.items) : []),
    [reports.data],
  );
  return (
    <SectionPanel title="Incident breakdown by category">
      {reports.isPending ? (
        <ListSkeleton rows={4} />
      ) : reports.isError ? (
        <ErrorState
          message={reports.error.message}
          onRetry={() => void reports.refetch()}
        />
      ) : items.length === 0 ? (
        <EmptyState
          title="No reports in this period"
          description="Try a longer time window."
        />
      ) : (
        <BarList items={items} />
      )}
    </SectionPanel>
  );
}
