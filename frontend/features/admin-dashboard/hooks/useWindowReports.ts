"use client";

import { useQuery } from "@tanstack/react-query";

import { windowReportsApi } from "../api/window-reports-api";
import type { StatsWindow } from "../lib/admin-stats";

export function useWindowReports(window: StatsWindow) {
  return useQuery({
    queryKey: ["admin", "overview", "window-reports", window],
    queryFn: ({ signal }) => windowReportsApi.activeIn(window, signal),
    staleTime: 60_000,
  });
}
