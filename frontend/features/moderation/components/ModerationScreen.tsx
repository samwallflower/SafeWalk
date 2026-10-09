"use client";

import { useState } from "react";

import { SectionPanel } from "@/components/shared/SectionPanel";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

import { useReportsByStatus, type ListedStatus } from "../hooks/useModeration";
import { ModerationTable } from "./ModerationTable";
import { ReportLookup } from "./ReportLookup";

const TABS: { status: ListedStatus; label: string; empty: string }[] = [
  {
    status: "UNDER_REVIEW",
    label: "Under review",
    empty: "Nothing is waiting for review",
  },
  { status: "HIDDEN", label: "Hidden", empty: "No reports are hidden" },
];

export function ModerationScreen() {
  const [status, setStatus] = useState<ListedStatus>("UNDER_REVIEW");
  const reports = useReportsByStatus(status);
  const tab = TABS.find((t) => t.status === status) ?? TABS[0];

  return (
    <div className="space-y-6">
      <SectionPanel title="Open a report">
        <ReportLookup />
      </SectionPanel>
      <SectionPanel
        title="Moderation queue"
        headerExtra={
          <div
            className="flex gap-1 rounded-xl bg-muted p-1"
            role="tablist"
            aria-label="Report status"
          >
            {TABS.map((t) => (
              <Button
                key={t.status}
                type="button"
                role="tab"
                aria-selected={t.status === status}
                variant="ghost"
                size="sm"
                className={cn(t.status === status && "bg-card shadow-sm")}
                onClick={() => setStatus(t.status)}
              >
                {t.label}
              </Button>
            ))}
          </div>
        }
      >
        <ModerationTable
          rows={reports.data}
          isLoading={reports.isPending}
          error={reports.error}
          onRetry={() => void reports.refetch()}
          emptyTitle={tab.empty}
          emptyDescription="Active reports are not listed here; open one by id above."
        />
      </SectionPanel>
    </div>
  );
}
