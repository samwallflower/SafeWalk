import type { LatLng } from "@/lib/geo/haversine";

export interface Circle extends LatLng {
  radiusMeters: number;
}

export interface Bounds {
  west: number;
  south: number;
  east: number;
  north: number;
}

export interface Viewport {
  bounds: Bounds;
  zoom: number;
}

export type TimeRange = "all" | "24h" | "7d" | "30d";

export interface MapFilterValues {
  categoryId: number | null;
  range: TimeRange;
}

export interface Coordinates {
  latitude: number;
  longitude: number;
}
