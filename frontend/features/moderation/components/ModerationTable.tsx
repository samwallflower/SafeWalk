"use client";

import { MoreHorizontalIcon } from "lucide-react";
import Link from "next/link";
import { useState } from "react";

import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { CategoryBadge } from "@/features/incidents/components/CategoryBadge";
import type { Incident, ReportStatus } from "@/features/incidents/types";
import { ReportStatusChip } from "@/features/my-reports/components/ReportStatusChip";
import { useStreetName } from "@/hooks/useStreetName";
import { formatDateTime } from "@/lib/format";

import { useDeleteAnyReport, useSetReportStatus } from "../hooks/useModeration";

function Location({ report }: { report: Incident }) {
  const street = useStreetName(report.latitude, report.longitude);
  return (
    <span>
      {street.data ??
        `${report.latitude.toFixed(4)}, ${report.longitude.toFixed(4)}`}
    </span>
  );
}

const ACTIONS: { status: ReportStatus; label: string }[] = [
  { status: "ACTIVE", label: "Make visible on map" },
  { status: "UNDER_REVIEW", label: "Mark under review" },
  { status: "HIDDEN", label: "Hide from map" },
];

interface ModerationTableProps {
  rows: readonly Incident[] | undefined;
  isLoading: boolean;
  error: Error | null;
  onRetry: () => void;
  emptyTitle: string;
  emptyDescription?: string;
}

export function ModerationTable({
  rows,
  isLoading,
  error,
  onRetry,
  emptyTitle,
  emptyDescription,
}: ModerationTableProps) {
  const setStatus = useSetReportStatus();
  const remove = useDeleteAnyReport();
  const [deleting, setDeleting] = useState<Incident | null>(null);

  const columns: readonly Column<Incident>[] = [
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
      key: "location",
      header: "Location",
      cell: (r) => <Location report={r} />,
    },
    {
      key: "when",
      header: "Reported",
      cell: (r) => (
        <span className="text-muted-foreground">
          {formatDateTime(r.timestamp)}
        </span>
      ),
    },
    {
      key: "votes",
      header: "Votes",
      cell: (r) => (
        <span className="font-medium">
          +{r.upvotes} / -{r.downvotes}
        </span>
      ),
    },
    {
      key: "status",
      header: "Status",
      cell: (r) => <ReportStatusChip status={r.status} />,
    },
    {
      key: "actions",
      header: "Actions",
      className: "text-right",
      cell: (r) => (
        <DropdownMenu>
          <DropdownMenuTrigger
            render={
              <Button
                variant="ghost"
                size="icon-sm"
                aria-label={`Actions for report ${r.id}`}
              />
            }
          >
            <MoreHorizontalIcon />
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            {ACTIONS.filter((a) => a.status !== r.status).map((a) => (
              <DropdownMenuItem
                key={a.status}
                disabled={setStatus.isPending}
                onClick={() => setStatus.mutate({ id: r.id, status: a.status })}
              >
                {a.label}
              </DropdownMenuItem>
            ))}
            <DropdownMenuItem onClick={() => setDeleting(r)}>
              Delete report
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      ),
    },
  ];

  return (
    <>
      <DataTable
        columns={columns}
        rows={rows}
        getKey={(r) => r.id}
        isLoading={isLoading}
        error={error}
        onRetry={onRetry}
        emptyTitle={emptyTitle}
        emptyDescription={emptyDescription}
      />
      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Delete this report?"
        description="It is removed permanently, including its votes."
        confirmLabel="Delete"
        isPending={remove.isPending}
        onConfirm={() =>
          deleting &&
          remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })
        }
      />
    </>
  );
}
