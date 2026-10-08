"use client";

import Link from "next/link";

import { DataTable, type Column } from "@/components/shared/DataTable";
import { CategoryBadge } from "@/features/incidents/components/CategoryBadge";
import type { Incident } from "@/features/incidents/types";
import { ReportStatusChip } from "@/features/my-reports/components/ReportStatusChip";
import { useStreetName } from "@/hooks/useStreetName";
import { formatDateTime } from "@/lib/format";

function Street({ report }: { report: Incident }) {
  const street = useStreetName(report.latitude, report.longitude);
  return (
    <span>
      {street.data ??
        `${report.latitude.toFixed(4)}, ${report.longitude.toFixed(4)}`}
    </span>
  );
}

const COLUMNS: readonly Column<Incident>[] = [
  {
    key: "id",
    header: "Report",
    cell: (r) => (
      <Link
        href={`/incidents/${r.id}`}
        className="font-semibold text-primary hover:underline"
      >
        #INC-{r.id}
      </Link>
    ),
  },
  {
    key: "category",
    header: "Category",
    cell: (r) => <CategoryBadge name={r.category.name} />,
  },
  {
    key: "description",
    header: "Description",
    className: "max-w-xs",
    cell: (r) => (
      <span className="line-clamp-2 break-words">{r.description}</span>
    ),
  },
  { key: "location", header: "Location", cell: (r) => <Street report={r} /> },
  {
    key: "when",
    header: "Reported",
    cell: (r) => (
      <span className="whitespace-nowrap text-muted-foreground">
        {formatDateTime(r.timestamp)}
      </span>
    ),
  },
  {
    key: "votes",
    header: "Votes",
    cell: (r) => (
      <span className="whitespace-nowrap font-medium">
        +{r.upvotes} / -{r.downvotes}
      </span>
    ),
  },
  {
    key: "status",
    header: "Status",
    cell: (r) => <ReportStatusChip status={r.status} />,
  },
];

interface ReportsTableProps {
  rows: readonly Incident[] | undefined;
  isLoading: boolean;
  error: Error | null;
  onRetry: () => void;
  filtered: boolean;
}

export function ReportsTable({
  rows,
  isLoading,
  error,
  onRetry,
  filtered,
}: ReportsTableProps) {
  return (
    <DataTable
      columns={COLUMNS}
      rows={rows}
      getKey={(r) => r.id}
      isLoading={isLoading}
      error={error}
      onRetry={onRetry}
      emptyTitle={
        filtered ? "No reports match these filters" : "No reports in this area"
      }
      emptyDescription={
        filtered
          ? "Clear the filters or widen the search radius."
          : "Try a bigger radius or a different place."
      }
    />
  );
}
