"use client";

import { useQuery } from "@tanstack/react-query";

import { browseApi } from "../api/browse-api";

export function useTotalReports() {
  return useQuery({
    queryKey: ["incidents", "count"],
    queryFn: ({ signal }) => browseApi.countAll(signal),
    staleTime: 5 * 60_000,
  });
}
