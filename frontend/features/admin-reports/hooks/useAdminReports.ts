"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";

import { reportsAdminApi, type StatusFilter } from "../api/reports-admin-api";

export const ADMIN_REPORTS_PAGE_SIZE = 20;

/** One page of reports for the admin table. `page` starts at 1. */
export function useAdminReportsPage(status: StatusFilter, page: number) {
  return useQuery({
    // Under the moderation key so status changes and deletes there refresh this table too.
    queryKey: ["admin", "moderation", "reports", status, page, ADMIN_REPORTS_PAGE_SIZE],
    queryFn: ({ signal }) => reportsAdminApi.page(status, page - 1, ADMIN_REPORTS_PAGE_SIZE, signal),
    placeholderData: keepPreviousData,
    staleTime: 30_000,
  });
}
