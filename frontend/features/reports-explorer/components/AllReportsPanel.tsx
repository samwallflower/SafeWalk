"use client";

import { useState } from "react";

import { useActivePage } from "../hooks/useActivePage";
import { ReportsTable } from "./ReportsTable";
import { TablePagination } from "./TablePagination";

const PAGE_SIZE = 25;

/** Every active report, paged by the server. Filters are not available here (they need a bounded area). */
export function AllReportsPanel() {
  const [page, setPage] = useState(1);
  const result = useActivePage(page, PAGE_SIZE);

  return (
    <>
      <p className="text-sm text-muted-foreground" aria-live="polite">
        {result.data
          ? `${result.data.total.toLocaleString()} active reports across SafeWalk`
          : result.isPending
            ? "Loading reports…"
            : null}
      </p>
      <section className="space-y-2 rounded-2xl bg-card p-4 shadow-sm ring-1 ring-foreground/5">
        <ReportsTable
          rows={result.data?.items}
          isLoading={result.isPending}
          error={result.error}
          onRetry={() => void result.refetch()}
          filtered={false}
        />
        {result.data ? (
          <TablePagination
            page={result.data.page}
            pageCount={result.data.pageCount}
            total={result.data.total}
            pageSize={PAGE_SIZE}
            onPage={setPage}
          />
        ) : null}
      </section>
    </>
  );
}
