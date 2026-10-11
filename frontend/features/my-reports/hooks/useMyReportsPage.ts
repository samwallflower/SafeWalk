"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";

import { useSession } from "@/features/auth/hooks/useSession";

import { myReportsApi } from "../api/my-reports-api";
import { myReportsKey } from "./useMyReports";

export const MY_REPORTS_PAGE_SIZE = 10;

/** One page of the signed-in person's reports. `page` starts at 1. */
export function useMyReportsPage(page: number) {
  const { user } = useSession();
  return useQuery({
    // Sits under the same key as the full list, so editing or deleting a report refreshes both.
    queryKey: [...myReportsKey(user?.id), "page", page, MY_REPORTS_PAGE_SIZE],
    queryFn: ({ signal }) => myReportsApi.page(user!.id, page - 1, MY_REPORTS_PAGE_SIZE, signal),
    enabled: user !== null,
    placeholderData: keepPreviousData,
  });
}
