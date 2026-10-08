"use client";

import { useQuery } from "@tanstack/react-query";

import { useDebounced } from "@/hooks/useDebounced";
import type { LatLng } from "@/lib/geo/haversine";
import { searchPlaces } from "@/lib/mapbox/geocode";

const MIN_QUERY_LENGTH = 3;

export function useGeocodeSearch(query: string, proximity?: LatLng) {
  const debounced = useDebounced(query.trim(), 300);
  const enabled = debounced.length >= MIN_QUERY_LENGTH;
  // Coarse rounding so small pans do not change the cache key.
  const nearKey = proximity ? [Number(proximity.latitude.toFixed(1)), Number(proximity.longitude.toFixed(1))] : null;
  return {
    ...useQuery({
      queryKey: ["geocode", debounced, nearKey],
      queryFn: ({ signal }) => searchPlaces(debounced, signal, proximity),
      enabled,
      staleTime: 5 * 60_000,
    }),
    enabled,
    settled: debounced === query.trim(),
  };
}
