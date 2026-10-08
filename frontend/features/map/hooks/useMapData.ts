"use client";

import { useMemo } from "react";

import type { HeatPoint, Incident } from "@/features/incidents/types";
import { useDebounced } from "@/hooks/useDebounced";

import { MAX_FILTER_RADIUS_M, MAX_HEATMAP_RADIUS_M, MAX_POINTS_RADIUS_M, MOVE_DEBOUNCE_MS } from "../config";
import { viewportCircle } from "../lib/area";
import { filterIncidents, filtersActive } from "../lib/filter-incidents";
import { incidentsToHeatPoints } from "../lib/geojson";
import type { MapFilterValues, Viewport } from "../types";
import { useFetchArea } from "./useFetchArea";
import { useHeatmapPoints } from "./useHeatmapPoints";
import { useNearbyIncidents } from "./useNearbyIncidents";

export interface MapData {
  /** Viewport is too large to request data safely. */
  zoomedOut: boolean;
  /** Filters are set but the view is too large to apply them. */
  filtersNeedZoom: boolean;
  heatPoints: HeatPoint[];
  incidents: Incident[];
  isLoading: boolean;
  isFetching: boolean;
  error: Error | null;
  retry: () => void;
}

const EMPTY_HEAT: HeatPoint[] = [];
const EMPTY_INCIDENTS: Incident[] = [];

export function useMapData(viewport: Viewport | null, filters: MapFilterValues): MapData {
  const debounced = useDebounced(viewport, MOVE_DEBOUNCE_MS);
  const view = useMemo(() => (debounced ? viewportCircle(debounced.bounds) : null), [debounced]);

  const zoomedOut = view !== null && view.radiusMeters > MAX_HEATMAP_RADIUS_M;
  const active = filtersActive(filters);
  const canFilter = view !== null && view.radiusMeters <= MAX_FILTER_RADIUS_M;
  const heatFromNearby = active && canFilter;
  const wantsPoints = view !== null && view.radiusMeters <= MAX_POINTS_RADIUS_M;

  const heatArea = useFetchArea(view, MAX_HEATMAP_RADIUS_M);
  const nearArea = useFetchArea(view, heatFromNearby ? MAX_FILTER_RADIUS_M : MAX_POINTS_RADIUS_M);

  const heatQuery = useHeatmapPoints(heatArea, !zoomedOut && !heatFromNearby);
  const nearQuery = useNearbyIncidents(nearArea, !zoomedOut && (heatFromNearby || wantsPoints));

  const nearData = nearQuery.data;
  const incidents = useMemo(
    () => (nearData && (wantsPoints || heatFromNearby) ? filterIncidents(nearData, filters) : EMPTY_INCIDENTS),
    [nearData, filters, wantsPoints, heatFromNearby],
  );
  const heatPoints = useMemo(
    () => (heatFromNearby ? incidentsToHeatPoints(incidents) : (heatQuery.data ?? EMPTY_HEAT)),
    [heatFromNearby, incidents, heatQuery.data],
  );

  const queries = [heatFromNearby ? null : heatQuery, wantsPoints || heatFromNearby ? nearQuery : null].filter(
    (q) => q !== null,
  );
  const failed = queries.find((q) => q.isError);

  return {
    zoomedOut,
    filtersNeedZoom: active && !canFilter && !zoomedOut,
    heatPoints: zoomedOut ? EMPTY_HEAT : heatPoints,
    incidents: zoomedOut || !wantsPoints ? EMPTY_INCIDENTS : incidents,
    isLoading: queries.some((q) => q.isPending && q.fetchStatus !== "idle"),
    isFetching: queries.some((q) => q.isFetching),
    error: failed?.error ?? null,
    retry: () => queries.forEach((q) => void q.refetch()),
  };
}
