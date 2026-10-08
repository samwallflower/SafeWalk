import { haversineMeters } from "@/lib/geo/haversine";

import { AREA_MAX_OVERSIZE, AREA_PADDING } from "../config";
import type { Bounds, Circle } from "../types";

/** Smallest circle (centered on the view) that covers the whole viewport rectangle. */
export function viewportCircle(bounds: Bounds): Circle {
  const center = {
    latitude: (bounds.north + bounds.south) / 2,
    longitude: (bounds.east + bounds.west) / 2,
  };
  const corner = { latitude: bounds.north, longitude: bounds.east };
  return { ...center, radiusMeters: haversineMeters(center, corner) };
}

export function padCircle(circle: Circle, factor: number = AREA_PADDING): Circle {
  return { ...circle, radiusMeters: Math.ceil(circle.radiusMeters * factor) };
}

export function circleContains(outer: Circle, inner: Circle): boolean {
  return haversineMeters(outer, inner) + inner.radiusMeters <= outer.radiusMeters;
}

/**
 * Decides which area to fetch for the current viewport circle.
 * - null: viewport is larger than `maxRadiusM`, so nothing may be fetched
 * - `previous`: still covers the view and is not wastefully large (no refetch)
 * - new padded circle otherwise
 */
export function nextFetchArea(previous: Circle | null, view: Circle | null, maxRadiusM: number): Circle | null {
  if (!view) return previous;
  if (view.radiusMeters > maxRadiusM) return null;
  if (
    previous &&
    circleContains(previous, view) &&
    previous.radiusMeters <= view.radiusMeters * AREA_PADDING * AREA_MAX_OVERSIZE
  ) {
    return previous;
  }
  return { ...padCircle(view), radiusMeters: Math.min(padCircle(view).radiusMeters, maxRadiusM) };
}

/** Rounded so equal-looking areas share a query key. */
export function areaKey(area: Circle) {
  return {
    latitude: Number(area.latitude.toFixed(5)),
    longitude: Number(area.longitude.toFixed(5)),
    radiusMeters: Math.round(area.radiusMeters),
  };
}
