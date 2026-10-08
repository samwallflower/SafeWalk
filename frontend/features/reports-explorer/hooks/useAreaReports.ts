"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";

import type { ReportStatus } from "@/features/incidents/types";

import { explorerApi } from "../api/explorer-api";

interface UseAreaReportsInput {
  latitude: number;
  longitude: number;
  radiusMeters: number;
  status: ReportStatus;
}

export function useAreaReports({
  latitude,
  longitude,
  radiusMeters,
  status,
}: UseAreaReportsInput) {
  const lat = Number(latitude.toFixed(5));
  const lng = Number(longitude.toFixed(5));
  return useQuery({
    queryKey: ["incidents", "explorer", lat, lng, radiusMeters, status],
    queryFn: ({ signal }) =>
      explorerApi.inArea(
        { latitude: lat, longitude: lng, radiusMeters },
        status,
        signal,
      ),
    placeholderData: keepPreviousData,
    staleTime: 60_000,
  });
}
