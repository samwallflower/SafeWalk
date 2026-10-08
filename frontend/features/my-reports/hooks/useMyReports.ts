"use client";

import { useQuery } from "@tanstack/react-query";

import { useSession } from "@/features/auth/hooks/useSession";

import { myReportsApi } from "../api/my-reports-api";

export const myReportsKey = (userId: number | undefined) => ["me", userId, "reports"] as const;

export function useMyReports() {
  const { user } = useSession();
  return useQuery({
    queryKey: myReportsKey(user?.id),
    queryFn: ({ signal }) => myReportsApi.list(user!.id, signal),
    enabled: user !== null,
    staleTime: 60_000,
    select: (reports) => [...reports].sort((a, b) => b.timestamp.localeCompare(a.timestamp)),
  });
}
