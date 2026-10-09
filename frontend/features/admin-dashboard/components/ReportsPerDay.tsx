"use client";

import { useMemo } from "react";

import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { ListSkeleton } from "@/components/shared/ListSkeleton";
import { MiniBars } from "@/components/shared/MiniBars";
import { SectionPanel } from "@/components/shared/SectionPanel";

import { useWindowReports } from "../hooks/useWindowReports";
import { countPerDay, type StatsWindow } from "../lib/admin-stats";

export function ReportsPerDay({ window }: { window: StatsWindow }) {
  const reports = useWindowReports(window);
  const points = useMemo(
    () => (reports.data ? countPerDay(reports.data.items) : []),
    [reports.data],
  );
  return (
    <SectionPanel title="Reports per day">
      {reports.isPending ? (
        <ListSkeleton rows={2} />
      ) : reports.isError ? (
        <ErrorState
          message={reports.error.message}
          onRetry={() => void reports.refetch()}
        />
      ) : points.length === 0 ? (
        <EmptyState title="No reports in this period" />
      ) : (
        <MiniBars points={points} />
      )}
    </SectionPanel>
  );
}
