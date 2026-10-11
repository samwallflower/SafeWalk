"use client";

import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { ListSkeleton } from "@/components/shared/ListSkeleton";

import { useMyReports } from "../hooks/useMyReports";
import { MyReportItem } from "./MyReportItem";

interface MyReportsListProps {
  /** How many of the newest reports to show. The full, editable, paged list is `MyReportsPaged`. */
  limit: number;
}

/** Read-only preview of the newest reports, for the dashboard. */
export function MyReportsList({ limit }: MyReportsListProps) {
  const reports = useMyReports();

  if (reports.isPending) return <ListSkeleton rows={limit} />;
  if (reports.isError) return <ErrorState message={reports.error.message} onRetry={() => void reports.refetch()} />;
  if (reports.data.length === 0) {
    return <EmptyState title="No reports yet" description="Incidents you report will appear here with how the community responded." />;
  }
  return (
    <ul className="space-y-3">
      {reports.data.slice(0, limit).map((report) => (
        <MyReportItem key={report.id} report={report} />
      ))}
    </ul>
  );
}
