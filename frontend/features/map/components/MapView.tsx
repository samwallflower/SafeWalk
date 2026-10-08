"use client";

import "mapbox-gl/dist/mapbox-gl.css";
import { useCallback, useState, type Ref } from "react";
import Map, {
  Marker,
  NavigationControl,
  type MapEvent,
  type MapMouseEvent,
  type MapRef,
  type ViewStateChangeEvent,
} from "react-map-gl/mapbox";

import { ErrorState } from "@/components/shared/ErrorState";
import type { Incident } from "@/features/incidents/types";
import { env } from "@/lib/env";

import { DEFAULT_VIEW } from "../config";
import { HIT_LAYER_ID } from "../lib/layer-styles";
import type { Coordinates, Viewport } from "../types";
import { HeatmapLayer } from "./HeatmapLayer";
import { IncidentPoints } from "./IncidentPoints";
import { IncidentPopup } from "./IncidentPopup";
import type { HeatPoint } from "@/features/incidents/types";

interface MapViewProps {
  mapRef: Ref<MapRef>;
  heatPoints: readonly HeatPoint[];
  incidents: readonly Incident[];
  userLocation: Coordinates | null;
  selectedId: number | null;
  hoveredId: number | null;
  onViewportChange: (viewport: Viewport) => void;
  onSelect: (id: number | null) => void;
  onHover: (id: number | null) => void;
}

function readViewport(target: MapEvent["target"]): Viewport | null {
  const b = target.getBounds();
  if (!b) return null;
  return {
    bounds: { west: b.getWest(), south: b.getSouth(), east: b.getEast(), north: b.getNorth() },
    zoom: target.getZoom(),
  };
}

function featureId(event: MapMouseEvent): number | null {
  const id: unknown = event.features?.[0]?.properties?.id;
  return typeof id === "number" ? id : null;
}

export function MapView({
  mapRef,
  heatPoints,
  incidents,
  userLocation,
  selectedId,
  hoveredId,
  onViewportChange,
  onSelect,
  onHover,
}: MapViewProps) {
  const [cursor, setCursor] = useState("");

  const emit = useCallback(
    (event: MapEvent | ViewStateChangeEvent) => {
      const viewport = readViewport(event.target);
      if (viewport) onViewportChange(viewport);
    },
    [onViewportChange],
  );

  if (!env.mapboxToken) {
    return (
      <div className="p-6">
        <ErrorState title="Map unavailable" message="NEXT_PUBLIC_MAPBOX_TOKEN is not configured." />
      </div>
    );
  }

  const selected = incidents.find((i) => i.id === selectedId) ?? null;
  const hovered =
    hoveredId !== selectedId ? (incidents.find((i) => i.id === hoveredId) ?? null) : null;

  return (
    <Map
      ref={mapRef}
      mapboxAccessToken={env.mapboxToken}
      initialViewState={DEFAULT_VIEW}
      mapStyle="mapbox://styles/mapbox/light-v11"
      interactiveLayerIds={[HIT_LAYER_ID]}
      cursor={cursor}
      onLoad={emit}
      onMoveEnd={emit}
      onClick={(e) => onSelect(featureId(e))}
      onMouseMove={(e) => {
        const id = featureId(e);
        setCursor(id === null ? "" : "pointer");
        onHover(id);
      }}
      onMouseLeave={() => onHover(null)}
    >
      <NavigationControl position="top-right" showCompass={false} />
      <HeatmapLayer points={heatPoints} />
      <IncidentPoints incidents={incidents} selectedId={selectedId} />
      {userLocation ? (
        <Marker latitude={userLocation.latitude} longitude={userLocation.longitude} color="#0b52b8" />
      ) : null}
      {hovered ? <IncidentPopup incident={hovered} variant="hover" onClose={() => onHover(null)} /> : null}
      {selected ? <IncidentPopup incident={selected} variant="selected" onClose={() => onSelect(null)} /> : null}
    </Map>
  );
}
