"use client";

import "mapbox-gl/dist/mapbox-gl.css";
import { useMemo, type Ref } from "react";
import Map, { Layer, Marker, NavigationControl, Source, type MapRef } from "react-map-gl/mapbox";

import { DEFAULT_VIEW } from "@/features/map/config";
import { HeatmapLayer } from "@/features/map/components/HeatmapLayer";
import type { HeatPoint } from "@/features/incidents/types";
import type { Place } from "@/features/places/types";
import { env } from "@/lib/env";

import { routesToGeoJson, type DecodedRoute } from "../lib/route-geometry";

interface RoutesMapProps {
  mapRef: Ref<MapRef>;
  routes: readonly DecodedRoute[];
  selectedId: number | null;
  hoveredId: number | null;
  origin: Place | null;
  destination: Place | null;
  incidents: readonly HeatPoint[];
  onSelect: (id: number) => void;
}

const LINE_LAYER_ID = "route-lines";

/** Draws every alternative; the selected/hovered one is thicker, opaque and on top. */
export function RoutesMap({ mapRef, routes, selectedId, hoveredId, origin, destination, incidents, onSelect }: RoutesMapProps) {
  const active = useMemo(() => new Set([selectedId, hoveredId].filter((id): id is number => id !== null)), [selectedId, hoveredId]);
  const data = useMemo(() => routesToGeoJson(routes, active), [routes, active]);

  if (!env.mapboxToken) {
    return <p className="p-6 text-sm text-muted-foreground">Map unavailable: Mapbox token is not configured.</p>;
  }

  return (
    <Map
      ref={mapRef}
      mapboxAccessToken={env.mapboxToken}
      initialViewState={DEFAULT_VIEW}
      mapStyle="mapbox://styles/mapbox/light-v11"
      interactiveLayerIds={[LINE_LAYER_ID]}
      onClick={(e) => {
        const id: unknown = e.features?.[0]?.properties?.routeId;
        if (typeof id === "number") onSelect(id);
      }}
    >
      <NavigationControl position="top-right" showCompass={false} />
      <HeatmapLayer points={incidents} />
      <Source id="routes" type="geojson" data={data}>
        <Layer
          id="route-casing"
          type="line"
          layout={{ "line-cap": "round", "line-join": "round" }}
          paint={{ "line-color": "#ffffff", "line-width": ["case", ["==", ["get", "active"], 1], 10, 7] }}
        />
        <Layer
          id={LINE_LAYER_ID}
          type="line"
          layout={{ "line-cap": "round", "line-join": "round" }}
          paint={{
            "line-color": ["get", "color"],
            "line-width": ["case", ["==", ["get", "active"], 1], 6, 4],
            "line-opacity": ["case", ["==", ["get", "active"], 1], 1, 0.6],
          }}
        />
      </Source>
      {origin ? (
        <Marker latitude={origin.latitude} longitude={origin.longitude}>
          <span className="block size-4 rounded-full border-[3px] border-white bg-primary shadow" aria-label="Start" />
        </Marker>
      ) : null}
      {destination ? <Marker latitude={destination.latitude} longitude={destination.longitude} color="#c4161c" /> : null}
    </Map>
  );
}
