"use client";

import "mapbox-gl/dist/mapbox-gl.css";
import type { Ref } from "react";
import Map, { Marker, NavigationControl, type MapRef } from "react-map-gl/mapbox";

import { DEFAULT_VIEW } from "@/features/map/config";
import { env } from "@/lib/env";
import type { LatLng } from "@/lib/geo/haversine";

interface LocationPinPickerProps {
  mapRef: Ref<MapRef>;
  value: LatLng;
  onChange: (position: LatLng) => void;
}

/** Draggable red pin; clicking the map moves it too. Coordinates stay in {latitude, longitude}. */
export function LocationPinPicker({ mapRef, value, onChange }: LocationPinPickerProps) {
  if (!env.mapboxToken) {
    return <p className="text-sm text-muted-foreground">Map unavailable: Mapbox token is not configured.</p>;
  }
  return (
    <div className="h-56 overflow-hidden rounded-xl md:h-64">
      <Map
        ref={mapRef}
        mapboxAccessToken={env.mapboxToken}
        initialViewState={{ latitude: value.latitude, longitude: value.longitude, zoom: DEFAULT_VIEW.zoom + 2 }}
        mapStyle="mapbox://styles/mapbox/light-v11"
        onClick={(e) => onChange({ latitude: e.lngLat.lat, longitude: e.lngLat.lng })}
      >
        <NavigationControl position="top-right" showCompass={false} />
        <Marker
          latitude={value.latitude}
          longitude={value.longitude}
          draggable
          onDragEnd={(e) => onChange({ latitude: e.lngLat.lat, longitude: e.lngLat.lng })}
        >
          <div className="relative flex cursor-grab items-center justify-center active:cursor-grabbing" aria-label="Incident location pin">
            <span className="absolute size-9 rounded-full bg-destructive/20" aria-hidden="true" />
            <span className="relative size-4 rounded-full border-[2.5px] border-white bg-destructive shadow" />
          </div>
        </Marker>
      </Map>
    </div>
  );
}
