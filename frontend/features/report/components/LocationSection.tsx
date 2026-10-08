"use client";

import { CheckCircle2Icon, MapPinIcon } from "lucide-react";
import { useRef } from "react";
import type { MapRef } from "react-map-gl/mapbox";

import { Chip } from "@/components/shared/Chip";
import { Button } from "@/components/ui/button";
import { useGeolocation } from "@/hooks/useGeolocation";
import { useReverseGeocode } from "@/hooks/useReverseGeocode";
import type { LatLng } from "@/lib/geo/haversine";

import { LocationPinPickerLazy } from "./LocationPinPickerLazy";
import { SectionCard } from "./SectionCard";

interface LocationSectionProps {
  value: LatLng;
  fromGps: boolean;
  onChange: (position: LatLng, fromGps: boolean) => void;
  error?: string;
}

function formatCoordinate(value: number, positive: string, negative: string): string {
  return `${Math.abs(value).toFixed(4)}° ${value >= 0 ? positive : negative}`;
}

const GPS_PROBLEMS = {
  denied: "Location permission denied. Drag the pin or click the map instead.",
  unavailable: "Location is not available on this device. Drag the pin or click the map instead.",
  error: "Could not get your location. Drag the pin or click the map instead.",
} as const;

export function LocationSection({ value, fromGps, onChange, error }: LocationSectionProps) {
  const mapRef = useRef<MapRef>(null);
  const geo = useGeolocation();
  const address = useReverseGeocode(value);
  const problem = geo.status in GPS_PROBLEMS ? GPS_PROBLEMS[geo.status as keyof typeof GPS_PROBLEMS] : null;

  const useMyLocation = () =>
    geo.locate((found) => {
      onChange(found, true);
      mapRef.current?.flyTo({ center: [found.longitude, found.latitude], zoom: 16, duration: 900 });
    });

  return (
    <SectionCard
      step={1}
      title="Incident Location"
      hint={undefined}
    >
      <div className="flex flex-wrap items-center gap-3 rounded-xl bg-muted p-3">
        <span className="rounded-lg bg-card p-2.5 text-primary">
          <MapPinIcon className="size-5" aria-hidden="true" />
        </span>
        <div className="min-w-0 flex-1">
          <p className="truncate font-semibold">{address.data ?? "Selected location"}</p>
          <p className="text-xs text-muted-foreground">
            Coordinates: {formatCoordinate(value.latitude, "N", "S")}, {formatCoordinate(value.longitude, "E", "W")}
          </p>
        </div>
        {fromGps ? (
          <Chip tone="success">
            <CheckCircle2Icon /> GPS accurate
          </Chip>
        ) : (
          <Chip>Placed manually</Chip>
        )}
        <Button type="button" variant="secondary" size="sm" className="bg-card" disabled={geo.status === "locating"} onClick={useMyLocation}>
          {geo.status === "locating" ? "Locating…" : "Use my location"}
        </Button>
      </div>
      {problem ? (
        <p role="alert" className="text-sm text-destructive">
          {problem}
        </p>
      ) : null}
      <LocationPinPickerLazy mapRef={mapRef} value={value} onChange={(position) => onChange(position, false)} />
      <p className="text-xs text-muted-foreground">Drag the pin or click the map to adjust the exact spot.</p>
      {error ? (
        <p role="alert" className="text-sm text-destructive">
          {error}
        </p>
      ) : null}
    </SectionCard>
  );
}
