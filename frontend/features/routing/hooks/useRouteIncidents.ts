"use client";

import { useMemo } from "react";

import { MAX_HEATMAP_RADIUS_M } from "@/features/map/config";
import { useHeatmapPoints } from "@/features/map/hooks/useHeatmapPoints";

import { coveringCircle, type DecodedRoute } from "../lib/route-geometry";

/** Incident dots under the routes. Skipped when the trip is too long to fetch safely. */
export function useRouteIncidents(routes: readonly DecodedRoute[]) {
  const area = useMemo(() => coveringCircle(routes.flatMap((r) => r.points)), [routes]);
  const withinLimit = area !== null && area.radiusMeters <= MAX_HEATMAP_RADIUS_M;
  return useHeatmapPoints(withinLimit ? area : null, withinLimit);
}
