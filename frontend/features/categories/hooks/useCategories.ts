"use client";

import { useQuery } from "@tanstack/react-query";

import { categoriesApi } from "../api/categories-api";

export function useCategories() {
  return useQuery({
    queryKey: ["categories", "list"],
    queryFn: ({ signal }) => categoriesApi.list(signal),
    staleTime: 10 * 60_000,
  });
}
