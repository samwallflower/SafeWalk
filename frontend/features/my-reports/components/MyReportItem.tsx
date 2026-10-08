"use client";

import { PencilIcon, ThumbsUpIcon, Trash2Icon } from "lucide-react";
import Link from "next/link";

import { Button } from "@/components/ui/button";
import { CategoryBadge } from "@/features/incidents/components/CategoryBadge";
import type { Incident } from "@/features/incidents/types";
import { formatDateTime } from "@/lib/format";

import { ReportStatusChip } from "./ReportStatusChip";

interface MyReportItemProps {
  report: Incident;
  onEdit?: (report: Incident) => void;
  onDelete?: (report: Incident) => void;
}

export function MyReportItem({ report, onEdit, onDelete }: MyReportItemProps) {
  return (
    <li className="space-y-3 rounded-xl bg-muted p-4">
      <div className="flex items-start justify-between gap-3">
        <div className="space-y-1.5">
          <CategoryBadge name={report.category.name} />
          <p className="text-sm leading-6 break-words">{report.description}</p>
        </div>
        <ReportStatusChip status={report.status} />
      </div>
      <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-muted-foreground">
        <span className="flex flex-wrap items-center gap-3">
          <span className="flex items-center gap-1 font-semibold text-primary">
            <ThumbsUpIcon className="size-3.5" aria-hidden="true" />
            {report.upvotes} upvotes
          </span>
          <span>{report.downvotes} downvotes</span>
          <span>Reported {formatDateTime(report.timestamp)}</span>
        </span>
        <span className="flex items-center gap-1">
          <Link href={`/incidents/${report.id}`} className="font-semibold text-primary hover:underline">
            View Map Pin
          </Link>
          {onEdit ? (
            <Button type="button" variant="ghost" size="icon-sm" aria-label="Edit report" onClick={() => onEdit(report)}>
              <PencilIcon />
            </Button>
          ) : null}
          {onDelete ? (
            <Button type="button" variant="ghost" size="icon-sm" aria-label="Delete report" onClick={() => onDelete(report)}>
              <Trash2Icon />
            </Button>
          ) : null}
        </span>
      </div>
    </li>
  );
}
