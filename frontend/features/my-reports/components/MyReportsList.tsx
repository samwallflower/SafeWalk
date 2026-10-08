"use client";

import { useState } from "react";

import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { ListSkeleton } from "@/components/shared/ListSkeleton";
import type { Incident } from "@/features/incidents/types";

import { useMyReports } from "../hooks/useMyReports";
import { useDeleteReport } from "../hooks/useReportMutations";
import { EditReportDialog } from "./EditReportDialog";
import { MyReportItem } from "./MyReportItem";

const PAGE_SIZE = 20;

interface MyReportsListProps {
  /** Preview mode (dashboard): first N reports, read-only. */
  limit?: number;
}

export function MyReportsList({ limit }: MyReportsListProps) {
  const reports = useMyReports();
  const remove = useDeleteReport();
  const [editing, setEditing] = useState<Incident | null>(null);
  const [deleting, setDeleting] = useState<Incident | null>(null);
  const [visible, setVisible] = useState(PAGE_SIZE);
  const manageable = limit === undefined;

  if (reports.isPending) return <ListSkeleton rows={limit ?? 4} />;
  if (reports.isError) return <ErrorState message={reports.error.message} onRetry={() => void reports.refetch()} />;
  if (reports.data.length === 0) {
    return <EmptyState title="No reports yet" description="Incidents you report will appear here with how the community responded." />;
  }

  // The backend returns every report at once, so the page only renders a slice.
  const shown = reports.data.slice(0, limit ?? visible);
  const remaining = reports.data.length - shown.length;
  return (
    <>
      <ul className="space-y-3">
        {shown.map((report) => (
          <MyReportItem
            key={report.id}
            report={report}
            onEdit={manageable ? setEditing : undefined}
            onDelete={manageable ? setDeleting : undefined}
          />
        ))}
      </ul>
      {manageable && remaining > 0 ? (
        <div className="flex justify-center pt-1">
          <Button type="button" variant="secondary" onClick={() => setVisible((v) => v + PAGE_SIZE)}>
            Show {Math.min(PAGE_SIZE, remaining)} more ({remaining.toLocaleString()} remaining)
          </Button>
        </div>
      ) : null}
      {manageable ? (
        <>
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
      ) : null}
    </>
  );
}
