"use client";

import { useMemo } from "react";
import { Layer, Source } from "react-map-gl/mapbox";

import type { Incident } from "@/features/incidents/types";

import { incidentsToGeoJson } from "../lib/geojson";
import { DEFAULT_DOT_MIN_ZOOM, hitLayerAt, selectedDotLayer, selectedHaloLayer } from "../lib/layer-styles";

interface IncidentPointsProps {
  incidents: readonly Incident[];
  selectedId: number | null;
  minZoom?: number;
}

/** Interactive layer: invisible hit targets plus the highlighted selection. */
export function IncidentPoints({ incidents, selectedId, minZoom = DEFAULT_DOT_MIN_ZOOM }: IncidentPointsProps) {
  const data = useMemo(() => incidentsToGeoJson(incidents), [incidents]);
  const hit = useMemo(() => hitLayerAt(minZoom), [minZoom]);
  const halo = useMemo(() => selectedHaloLayer(selectedId), [selectedId]);
  const dot = useMemo(() => selectedDotLayer(selectedId), [selectedId]);
  return (
    <Source id="incident-interactive" type="geojson" data={data}>
      <Layer {...hit} />
      <Layer {...halo} />
      <Layer {...dot} />
    </Source>
  );
}
