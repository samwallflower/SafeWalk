"use client";

import { useQuery } from "@tanstack/react-query";

import { useSession } from "@/features/auth/hooks/useSession";

import { sessionsApi } from "../api/sessions-api";

export function useMySessions() {
  const { user } = useSession();
  return useQuery({
    queryKey: ["me", user?.id, "sessions"],
    queryFn: ({ signal }) => sessionsApi.listMine(user!.id, signal),
    enabled: user !== null,
    staleTime: 60_000,
    select: (sessions) => [...sessions].sort((a, b) => b.startTime.localeCompare(a.startTime)),
  });
}
