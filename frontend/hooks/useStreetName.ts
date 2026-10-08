"use client";

import { useQuery } from "@tanstack/react-query";

import { env } from "@/lib/env";
import { reverseStreet } from "@/lib/mapbox/geocode";

/** Street name for a coordinate via Mapbox. Cached per rounded location; only use for visible items. */
export function useStreetName(latitude: number | null | undefined, longitude: number | null | undefined) {
  const has = typeof latitude === "number" && typeof longitude === "number";
  const lat = has ? Number(latitude.toFixed(5)) : null;
  const lng = has ? Number(longitude.toFixed(5)) : null;
  return useQuery({
    queryKey: ["geocode", "street", lat, lng],
    queryFn: ({ signal }) => reverseStreet(lat as number, lng as number, signal),
    enabled: has && env.mapboxToken !== "",
    staleTime: Infinity,
    gcTime: 60 * 60_000,
  });
}
