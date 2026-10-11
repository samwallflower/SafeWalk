"use client";

import { useState } from "react";

import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { ListSkeleton } from "@/components/shared/ListSkeleton";
import { TablePagination } from "@/features/reports-explorer/components/TablePagination";
import type { Incident } from "@/features/incidents/types";

import { MY_REPORTS_PAGE_SIZE, useMyReportsPage } from "../hooks/useMyReportsPage";
import { useDeleteReport } from "../hooks/useReportMutations";
import { EditReportDialog } from "./EditReportDialog";
import { MyReportItem } from "./MyReportItem";

/** The full "My reports" list: one server page at a time, newest first, with numbered paging. */
export function MyReportsPaged() {
  const [page, setPage] = useState(1);
  const reports = useMyReportsPage(page);
  const remove = useDeleteReport();
  const [editing, setEditing] = useState<Incident | null>(null);
  const [deleting, setDeleting] = useState<Incident | null>(null);

  if (reports.isPending) return <ListSkeleton rows={4} />;
  if (reports.isError) return <ErrorState message={reports.error.message} onRetry={() => void reports.refetch()} />;

  const { items, total, totalPages } = reports.data;
  // Deleting the last report on the final page leaves it empty: step back one page.
  if (items.length === 0 && page > 1) {
    setPage(Math.max(1, totalPages));
    return <ListSkeleton rows={4} />;
  }
  if (total === 0) {
    return <EmptyState title="No reports yet" description="Incidents you report will appear here with how the community responded." />;
  }

  return (
    <>
      <ul className="space-y-3" aria-busy={reports.isFetching}>
        {items.map((report) => (
          <MyReportItem key={report.id} report={report} onEdit={setEditing} onDelete={setDeleting} />
        ))}
      </ul>
      <TablePagination page={page} pageCount={Math.max(1, totalPages)} total={total} pageSize={MY_REPORTS_PAGE_SIZE} onPage={setPage} />
      <EditReportDialog report={editing} onOpenChange={(open) => !open && setEditing(null)} />
      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Delete this report?"
        description="It will be removed from the map for everyone. This cannot be undone."
        confirmLabel="Delete"
        isPending={remove.isPending}
        onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })}
      />
    </>
  );
}
