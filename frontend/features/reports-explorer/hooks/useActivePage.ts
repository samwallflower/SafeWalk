"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";

import { browseApi } from "../api/browse-api";

export function useActivePage(page: number, pageSize: number) {
  return useQuery({
    queryKey: ["incidents", "browse", "active", page, pageSize],
    queryFn: ({ signal }) => browseApi.activePage(page, pageSize, signal),
    placeholderData: keepPreviousData,
    staleTime: 60_000,
  });
}
