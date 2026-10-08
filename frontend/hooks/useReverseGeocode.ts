"use client";

import { useQuery } from "@tanstack/react-query";

import type { LatLng } from "@/lib/geo/haversine";
import { reversePlace } from "@/lib/mapbox/geocode";

export function useReverseGeocode(position: LatLng | null) {
  const lat = position ? Number(position.latitude.toFixed(5)) : null;
  const lng = position ? Number(position.longitude.toFixed(5)) : null;
  return useQuery({
    queryKey: ["geocode", "reverse", lat, lng],
    queryFn: ({ signal }) => reversePlace(lat as number, lng as number, signal),
    enabled: position !== null,
    staleTime: 10 * 60_000,
  });
}
