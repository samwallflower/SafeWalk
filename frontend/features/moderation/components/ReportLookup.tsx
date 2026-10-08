"use client";

import { SearchIcon } from "lucide-react";
import { useState } from "react";

import { ErrorState } from "@/components/shared/ErrorState";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ApiError } from "@/lib/http/api-error";

import { useReportLookup } from "../hooks/useModeration";
import { ModerationTable } from "./ModerationTable";

/** Open any report by id, including active ones, without ever listing them. */
export function ReportLookup() {
  const [input, setInput] = useState("");
  const [id, setId] = useState<number | null>(null);
  const report = useReportLookup(id);
  const notFound = report.error instanceof ApiError && report.error.status === 404;

  return (
    <div className="space-y-3">
      <form
        className="flex gap-2"
        onSubmit={(e) => {
          e.preventDefault();
          const parsed = Number(input.trim().replace(/^#?(INC-)?/i, ""));
          setId(Number.isInteger(parsed) && parsed > 0 ? parsed : null);
        }}
      >
        <div className="relative flex-1">
          <SearchIcon className="pointer-events-none absolute top-1/2 left-2.5 size-4 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
          <Input
            aria-label="Report id"
            inputMode="numeric"
            placeholder="Open a report by id, e.g. 78334"
            className="h-10 border-transparent bg-muted pl-8"
            value={input}
            onChange={(e) => setInput(e.target.value)}
          />
        </div>
        <Button type="submit" className="h-10">
          Open
        </Button>
      </form>
      {id !== null ? (
        report.isError && !notFound ? (
          <ErrorState message={report.error.message} onRetry={() => void report.refetch()} />
        ) : (
          <ModerationTable
            rows={report.data ? [report.data] : undefined}
            isLoading={report.isPending}
            error={null}
            onRetry={() => void report.refetch()}
            emptyTitle="Report not found"
            emptyDescription={`There is no report with id ${id}.`}
          />
        )
      ) : null}
    </div>
  );
}
