"use client";

import { EyeIcon, MoreVerticalIcon, PencilIcon, PlusIcon, Trash2Icon } from "lucide-react";
import Link from "next/link";
import { useState } from "react";

import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button, buttonVariants } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { CategoryBadge } from "@/features/incidents/components/CategoryBadge";
import type { Incident, ReportStatus } from "@/features/incidents/types";
import { useDeleteAnyReport, useSetReportStatus } from "@/features/moderation/hooks/useModeration";
import { ReportStatusChip } from "@/features/my-reports/components/ReportStatusChip";
import { TablePagination } from "@/features/reports-explorer/components/TablePagination";
import { useStreetName } from "@/hooks/useStreetName";
import { formatDateTime } from "@/lib/format";
import { cn } from "@/lib/utils";

import type { AdminReport, StatusFilter } from "../api/reports-admin-api";
import { ADMIN_REPORTS_PAGE_SIZE, useAdminReportsPage } from "../hooks/useAdminReports";
import { AdminEditReportDialog } from "./AdminEditReportDialog";

const FILTERS: readonly { value: StatusFilter; label: string }[] = [
  { value: "ALL", label: "All" },
  { value: "ACTIVE", label: "Active" },
  { value: "UNDER_REVIEW", label: "Under review" },
  { value: "HIDDEN", label: "Hidden" },
];

const STATUS_ACTIONS: readonly { status: ReportStatus; label: string }[] = [
  { status: "ACTIVE", label: "Make visible on map" },
  { status: "UNDER_REVIEW", label: "Mark under review" },
  { status: "HIDDEN", label: "Hide from map" },
];

function Location({ report }: { report: Incident }) {
  const street = useStreetName(report.latitude, report.longitude);
  return <span>{street.data ?? `${report.latitude.toFixed(4)}, ${report.longitude.toFixed(4)}`}</span>;
}

export function AdminReportsTable() {
  const [status, setStatus] = useState<StatusFilter>("ALL");
  const [page, setPage] = useState(1);
  const reports = useAdminReportsPage(status, page);
  const setReportStatus = useSetReportStatus();
  const remove = useDeleteAnyReport();
  const [deleting, setDeleting] = useState<Incident | null>(null);
  const [editing, setEditing] = useState<AdminReport | null>(null);

  const changeFilter = (next: StatusFilter) => {
    setStatus(next);
    setPage(1);
  };

  const columns: readonly Column<AdminReport>[] = [
    {
      key: "id",
      header: "Report",
      cell: (r) => (
        <Link href={`/incidents/${r.id}`} className="font-semibold text-primary hover:underline">
          #INC-{r.id}
        </Link>
      ),
    },
    { key: "category", header: "Category", cell: (r) => <CategoryBadge name={r.category.name} /> },
    { key: "description", header: "What happened", cell: (r) => <span className="line-clamp-2 max-w-72 text-muted-foreground">{r.description}</span> },
    { key: "location", header: "Location", cell: (r) => <Location report={r} /> },
    { key: "when", header: "Reported", cell: (r) => <span className="text-muted-foreground">{formatDateTime(r.timestamp)}</span> },
    { key: "votes", header: "Votes", cell: (r) => <span className="font-medium">+{r.upvotes} / -{r.downvotes}</span> },
    { key: "status", header: "Status", cell: (r) => <ReportStatusChip status={r.status} /> },
    {
      key: "actions",
      header: "Actions",
      className: "text-right",
      cell: (r) => (
        <span className="flex justify-end gap-1">
          <Link href={`/incidents/${r.id}`} className={cn(buttonVariants({ variant: "ghost", size: "icon-sm" }))} aria-label={`View report ${r.id}`}>
            <EyeIcon />
          </Link>
          <Button type="button" variant="ghost" size="icon-sm" aria-label={`Edit report ${r.id}`} disabled={r.reporterId === null} onClick={() => setEditing(r)}>
            <PencilIcon />
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger render={<Button variant="ghost" size="icon-sm" aria-label={`More actions for report ${r.id}`} />}>
              <MoreVerticalIcon />
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {STATUS_ACTIONS.filter((a) => a.status !== r.status).map((a) => (
                <DropdownMenuItem key={a.status} disabled={setReportStatus.isPending} onClick={() => setReportStatus.mutate({ id: r.id, status: a.status })}>
                  {a.label}
                </DropdownMenuItem>
              ))}
              <DropdownMenuSeparator />
              <DropdownMenuItem variant="destructive" onClick={() => setDeleting(r)}>
                <Trash2Icon /> Delete report
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </span>
      ),
    },
  ];

  const totalPages = reports.data?.totalPages ?? 1;

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex gap-1 rounded-lg border bg-card p-1" role="group" aria-label="Report status">
          {FILTERS.map((f) => (
            <Button
              key={f.value}
              type="button"
              variant="ghost"
              size="sm"
              aria-pressed={f.value === status}
              className={cn("rounded-md", f.value === status ? "bg-leaf font-bold text-foreground hover:bg-leaf" : "text-muted-foreground")}
              onClick={() => changeFilter(f.value)}
            >
              {f.label}
            </Button>
          ))}
        </div>
        <Link href="/report" className={cn(buttonVariants(), "h-9")}>
          <PlusIcon /> New report
        </Link>
      </div>
      <DataTable
        columns={columns}
        rows={reports.data?.items}
        getKey={(r) => r.id}
        isLoading={reports.isPending}
        error={reports.error}
        onRetry={() => void reports.refetch()}
        emptyTitle="No reports here"
        emptyDescription="Try another status."
      />
      {reports.data ? (
        <TablePagination page={page} pageCount={Math.max(1, totalPages)} total={reports.data.total} pageSize={ADMIN_REPORTS_PAGE_SIZE} onPage={setPage} />
      ) : null}
      <AdminEditReportDialog report={editing} onOpenChange={(open) => !open && setEditing(null)} />
      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Delete this report?"
        description="It is removed for everyone, including the map. This can't be undone."
        confirmLabel="Delete"
        isPending={remove.isPending}
        onConfirm={() => deleting && remove.mutate(deleting.id, { onSettled: () => setDeleting(null) })}
      />
    </div>
  );
}
