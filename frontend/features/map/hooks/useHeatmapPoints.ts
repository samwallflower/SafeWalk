"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";

import { incidentsApi } from "@/features/incidents/api/incidents-api";

import { areaKey } from "../lib/area";
import type { Circle } from "../types";

export function useHeatmapPoints(area: Circle | null, enabled: boolean) {
  return useQuery({
    queryKey: ["incidents", "heatmap", area ? areaKey(area) : null],
    queryFn: ({ signal }) => incidentsApi.heatmapPoints(areaKey(area as Circle), signal),
    enabled: enabled && area !== null,
    placeholderData: keepPreviousData,
    staleTime: 60_000,
  });
}
