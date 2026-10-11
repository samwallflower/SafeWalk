"use client";

import { useMemo } from "react";

import { BarList } from "@/components/shared/BarList";
import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { ListSkeleton } from "@/components/shared/ListSkeleton";
import { SectionPanel } from "@/components/shared/SectionPanel";
import { useMyReports } from "@/features/my-reports/hooks/useMyReports";

import { reportsByCategory } from "../lib/user-stats";

export function ReportsByCategoryPanel() {
  const reports = useMyReports();
  const items = useMemo(() => (reports.data ? reportsByCategory(reports.data) : []), [reports.data]);
  return (
    <SectionPanel title="My reports by category">
      {reports.isPending ? (
        <ListSkeleton rows={3} />
      ) : reports.isError ? (
        <ErrorState message={reports.error.message} onRetry={() => void reports.refetch()} />
      ) : items.length === 0 ? (
        <EmptyState title="No reports yet" description="Report an incident and the categories you use will show here." />
      ) : (
        <BarList items={items.map((i) => ({ ...i, color: "#8be284" }))} />
      )}
    </SectionPanel>
  );
}
