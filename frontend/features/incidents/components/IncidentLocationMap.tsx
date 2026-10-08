"use client";

import "mapbox-gl/dist/mapbox-gl.css";
import Map, { Layer, Source } from "react-map-gl/mapbox";

import { env } from "@/lib/env";

import { MARKER_COLOR } from "@/features/map/lib/layer-styles";

interface IncidentLocationMapProps {
  latitude: number;
  longitude: number;
}

export function IncidentLocationMap({ latitude, longitude }: IncidentLocationMapProps) {
  if (!env.mapboxToken) {
    return <p className="text-sm text-muted-foreground">Map unavailable: Mapbox token is not configured.</p>;
  }
  return (
    <div className="h-64 overflow-hidden rounded-xl">
      <Map
        mapboxAccessToken={env.mapboxToken}
        initialViewState={{ latitude, longitude, zoom: 15.5 }}
        mapStyle="mapbox://styles/mapbox/light-v11"
        interactive={false}
      >
        <Source
          id="detail-point"
          type="geojson"
          data={{
            type: "Feature",
            properties: {},
            geometry: { type: "Point", coordinates: [longitude, latitude] },
          }}
        >
          <Layer id="detail-halo" type="circle" paint={{ "circle-radius": 22, "circle-color": MARKER_COLOR, "circle-opacity": 0.18 }} />
          <Layer
            id="detail-core"
            type="circle"
            paint={{
              "circle-radius": 8,
              "circle-color": MARKER_COLOR,
              "circle-stroke-color": "#ffffff",
              "circle-stroke-width": 2.5,
            }}
          />
        </Source>
      </Map>
    </div>
  );
}
