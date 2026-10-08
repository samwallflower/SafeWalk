import type { FeatureCollection, LineString } from "geojson";

import { haversineMeters, type LatLng } from "@/lib/geo/haversine";
import { decodePolyline } from "@/lib/geo/polyline";

import type { RouteDto } from "../types";

export const RECOMMENDED_COLOR = "#0a6b32";
const ALTERNATIVE_COLORS = ["#0b52b8", "#9a6700", "#5b6475", "#7c3aed"] as const;

export interface DecodedRoute {
  route: RouteDto;
  points: LatLng[];
  color: string;
}

export interface RouteLineProperties {
  routeId: number;
  color: string;
  active: 0 | 1;
}

export function routeColor(rank: number): string {
  return rank === 1 ? RECOMMENDED_COLOR : ALTERNATIVE_COLORS[(rank - 2) % ALTERNATIVE_COLORS.length];
}

/** Sorted best-first and decoded once. */
export function decodeRoutes(routes: readonly RouteDto[]): DecodedRoute[] {
  return [...routes]
    .sort((a, b) => a.rank - b.rank)
    .map((route) => ({ route, points: decodePolyline(route.polyline), color: routeColor(route.rank) }));
}

/** Active routes are emitted last so they draw on top. GeoJSON order is [lng, lat]. */
export function routesToGeoJson(
  routes: readonly DecodedRoute[],
  activeIds: ReadonlySet<number>,
): FeatureCollection<LineString, RouteLineProperties> {
  const features = routes
    .filter((r) => r.points.length >= 2)
    .map((r) => ({
      type: "Feature" as const,
      properties: { routeId: r.route.id, color: r.color, active: (activeIds.has(r.route.id) ? 1 : 0) as 0 | 1 },
      geometry: { type: "LineString" as const, coordinates: r.points.map((p) => [p.longitude, p.latitude]) },
    }));
  return { type: "FeatureCollection", features: features.sort((a, b) => a.properties.active - b.properties.active) };
}

export interface LngLatBoundsTuple {
  southWest: [number, number];
  northEast: [number, number];
}

export function boundsOf(points: readonly LatLng[]): LngLatBoundsTuple | null {
  if (points.length === 0) return null;
  let west = Infinity;
  let south = Infinity;
  let east = -Infinity;
  let north = -Infinity;
  for (const p of points) {
    west = Math.min(west, p.longitude);
    east = Math.max(east, p.longitude);
    south = Math.min(south, p.latitude);
    north = Math.max(north, p.latitude);
  }
  return { southWest: [west, south], northEast: [east, north] };
}

/** Circle covering every route, used to load nearby incidents under the routes. */
export function coveringCircle(points: readonly LatLng[]): (LatLng & { radiusMeters: number }) | null {
  const bounds = boundsOf(points);
  if (!bounds) return null;
  const center = {
    latitude: (bounds.southWest[1] + bounds.northEast[1]) / 2,
    longitude: (bounds.southWest[0] + bounds.northEast[0]) / 2,
  };
  const radius = Math.max(...points.map((p) => haversineMeters(center, p)));
  return { ...center, radiusMeters: Math.ceil(radius) + 300 };
}
