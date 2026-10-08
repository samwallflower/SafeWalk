"use client";

import { useMemo } from "react";
import { Layer, Source } from "react-map-gl/mapbox";

import type { HeatPoint } from "@/features/incidents/types";

import { heatPointsToGeoJson } from "../lib/geojson";
import { DEFAULT_DOT_MIN_ZOOM, densityLayer, dotLayerAt, haloLayerAt } from "../lib/layer-styles";

interface HeatmapLayerProps {
  points: readonly HeatPoint[];
  /** Zoom at which dots appear. Lower it for small, already-bounded result sets. */
  minZoom?: number;
  /** Soft density wash for zoomed-out views of large sets. */
  showDensity?: boolean;
}

/** Visual layers for every known incident position: optional density wash, then halo + dot. */
export function HeatmapLayer({ points, minZoom = DEFAULT_DOT_MIN_ZOOM, showDensity = true }: HeatmapLayerProps) {
  const data = useMemo(() => heatPointsToGeoJson(points), [points]);
  const halo = useMemo(() => haloLayerAt(minZoom), [minZoom]);
  const dot = useMemo(() => dotLayerAt(minZoom), [minZoom]);
  return (
    <Source id="incident-positions" type="geojson" data={data}>
      {showDensity ? <Layer {...densityLayer} /> : null}
      <Layer {...halo} />
      <Layer {...dot} />
    </Source>
  );
}
