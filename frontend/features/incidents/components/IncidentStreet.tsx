"use client";

import { MapPinIcon } from "lucide-react";

import { useStreetName } from "@/hooks/useStreetName";

interface IncidentStreetProps {
  latitude: number;
  longitude: number;
}

/** Street name via Mapbox reverse geocoding; renders nothing while loading, on error, or if unknown. */
export function IncidentStreet({ latitude, longitude }: IncidentStreetProps) {
  const street = useStreetName(latitude, longitude);
  if (!street.data) return null;
  return (
    <span className="flex items-center gap-1 text-xs text-muted-foreground">
      <MapPinIcon className="size-3.5" aria-hidden="true" />
      {street.data}
    </span>
  );
}
