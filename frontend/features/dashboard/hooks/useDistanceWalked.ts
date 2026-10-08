"use client";

import { useQueries } from "@tanstack/react-query";
import { useMemo } from "react";

import { routingApi } from "@/features/routing/api/routing-api";
import { useMySessions } from "@/features/sessions/hooks/useMySessions";

import { completedRouteIds, totalWalkedMeters } from "../lib/walked-distance";

/**
 * Distance walked = sum of `actualDistanceMeters` of the routes behind the user's completed walks.
 * Routes never change once created, so they are cached for the whole session.
 */
export function useDistanceWalked() {
  const sessions = useMySessions();
  const { routeIds, capped } = useMemo(
    () => completedRouteIds(sessions.data ?? []),
    [sessions.data],
  );
  const distinctIds = useMemo(() => [...new Set(routeIds)], [routeIds]);

  const routes = useQueries({
    queries: distinctIds.map((id) => ({
      queryKey: ["routes", id],
      queryFn: ({ signal }: { signal: AbortSignal }) =>
        routingApi.getById(id, signal),
      staleTime: Infinity,
    })),
  });

  const loaded = new Map(
    routes.flatMap((q) => (q.data ? [[q.data.id, q.data] as const] : [])),
  );
  return {
    meters: totalWalkedMeters(routeIds, loaded),
    walks: routeIds.length,
    capped,
    isPending: sessions.isPending || routes.some((q) => q.isPending),
    isError: sessions.isError || routes.some((q) => q.isError),
  };
}
