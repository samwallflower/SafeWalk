"use client";

import "mapbox-gl/dist/mapbox-gl.css";
import { useMemo, useState } from "react";
import Map, {
  NavigationControl,
  type MapMouseEvent,
} from "react-map-gl/mapbox";

import { HeatmapLayer } from "@/features/map/components/HeatmapLayer";
import { IncidentPoints } from "@/features/map/components/IncidentPoints";
import { IncidentPopup } from "@/features/map/components/IncidentPopup";
import { incidentsToHeatPoints } from "@/features/map/lib/geojson";
import { HIT_LAYER_ID } from "@/features/map/lib/layer-styles";
import type { Incident } from "@/features/incidents/types";
import { env } from "@/lib/env";

interface ReportsMapProps {
  reports: readonly Incident[];
  latitude: number;
  longitude: number;
  radiusMeters: number;
}

/** Roughly fits the search circle. 1 degree of latitude is ~111 km. */
function boundsFor(
  latitude: number,
  longitude: number,
  radiusMeters: number,
): [[number, number], [number, number]] {
  const dLat = radiusMeters / 111_000;
  const dLng =
    radiusMeters /
    (111_000 * Math.max(0.2, Math.cos((latitude * Math.PI) / 180)));
  return [
    [longitude - dLng, latitude - dLat],
    [longitude + dLng, latitude + dLat],
  ];
}

/** Dots for the already-bounded result set, so they show from a low zoom. */
export function ReportsMap({
  reports,
  latitude,
  longitude,
  radiusMeters,
}: ReportsMapProps) {
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const points = useMemo(() => incidentsToHeatPoints(reports), [reports]);
  const selected = reports.find((r) => r.id === selectedId) ?? null;

  if (!env.mapboxToken) {
    return (
      <p className="p-6 text-sm text-muted-foreground">
        Map unavailable: Mapbox token is not configured.
      </p>
    );
  }

  const onClick = (e: MapMouseEvent) => {
    const id: unknown = e.features?.[0]?.properties?.id;
    setSelectedId(typeof id === "number" ? id : null);
  };

  return (
    <Map
      // A new search area remounts the map so it re-fits to the circle.
      key={`${latitude.toFixed(4)}:${longitude.toFixed(4)}:${radiusMeters}`}
      mapboxAccessToken={env.mapboxToken}
      initialViewState={{
        bounds: boundsFor(latitude, longitude, radiusMeters),
        fitBoundsOptions: { padding: 24 },
      }}
      mapStyle="mapbox://styles/mapbox/light-v11"
      interactiveLayerIds={[HIT_LAYER_ID]}
      onClick={onClick}
    >
      <NavigationControl position="top-right" showCompass={false} />
      <HeatmapLayer points={points} minZoom={8} showDensity={false} />
      <IncidentPoints incidents={reports} selectedId={selectedId} minZoom={8} />
      {selected ? (
        <IncidentPopup
          incident={selected}
          variant="selected"
          onClose={() => setSelectedId(null)}
        />
      ) : null}
    </Map>
  );
}
