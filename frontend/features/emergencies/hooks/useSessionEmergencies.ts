"use client";

import { useQuery } from "@tanstack/react-query";

import { useSession } from "@/features/auth/hooks/useSession";

import { emergenciesApi } from "../api/emergencies-api";

/** Loaded on demand (when a session is expanded); emergencies can only be fetched per session. */
export function useSessionEmergencies(sessionId: number, enabled: boolean) {
  const { user } = useSession();
  return useQuery({
    queryKey: ["me", user?.id, "emergencies", sessionId],
    queryFn: ({ signal }) => emergenciesApi.forSession(user!.id, sessionId, signal),
    enabled: enabled && user !== null,
    staleTime: 60_000,
  });
}
