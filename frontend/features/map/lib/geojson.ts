import type { FeatureCollection, Point } from "geojson";

import type { HeatPoint, Incident } from "@/features/incidents/types";

export interface HeatProperties {
  severityWeight: number;
}

export interface IncidentProperties {
  id: number;
  severityWeight: number;
}

/** GeoJSON/Mapbox coordinate order is [lng, lat]. */
export function heatPointsToGeoJson(points: readonly HeatPoint[]): FeatureCollection<Point, HeatProperties> {
  return {
    type: "FeatureCollection",
    features: points.map((p) => ({
      type: "Feature",
      properties: { severityWeight: p.severityWeight },
      geometry: { type: "Point", coordinates: [p.longitude, p.latitude] },
    })),
  };
}

export function incidentsToGeoJson(incidents: readonly Incident[]): FeatureCollection<Point, IncidentProperties> {
  return {
    type: "FeatureCollection",
    features: incidents.map((i) => ({
      type: "Feature",
      properties: { id: i.id, severityWeight: i.category.severityWeight },
      geometry: { type: "Point", coordinates: [i.longitude, i.latitude] },
    })),
  };
}

export function incidentsToHeatPoints(incidents: readonly Incident[]): HeatPoint[] {
  return incidents.map((i) => ({
    latitude: i.latitude,
    longitude: i.longitude,
    severityWeight: i.category.severityWeight,
  }));
}
