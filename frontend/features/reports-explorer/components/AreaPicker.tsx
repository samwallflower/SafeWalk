"use client";

import { LocateFixedIcon } from "lucide-react";
import { useState } from "react";

import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { PlaceSearch } from "@/features/places/components/PlaceSearch";
import type { Place } from "@/features/places/types";
import { useGeolocation } from "@/hooks/useGeolocation";
import type { LatLng } from "@/lib/geo/haversine";

export const RADIUS_OPTIONS: readonly { value: string; label: string }[] = [
  { value: "250", label: "250 m" },
  { value: "500", label: "500 m" },
  { value: "1000", label: "1 km" },
  { value: "2000", label: "2 km" },
];

interface AreaPickerProps {
  center: Place;
  radiusMeters: number;
  onCenter: (place: Place) => void;
  onRadius: (meters: number) => void;
}

const GEO_PROBLEMS = {
  denied: "Location permission denied. Search for a place instead.",
  unavailable: "Location is not available on this device.",
  error: "Could not get your location.",
} as const;

export function AreaPicker({
  center,
  radiusMeters,
  onCenter,
  onRadius,
}: AreaPickerProps) {
  const [query, setQuery] = useState(center.label);
  const geo = useGeolocation();
  const problem =
    geo.status in GEO_PROBLEMS
      ? GEO_PROBLEMS[geo.status as keyof typeof GEO_PROBLEMS]
      : null;
  const proximity: LatLng = center;

  return (
    <div className="space-y-2">
      <div className="grid gap-2 md:grid-cols-[minmax(0,1fr)_auto_8rem] md:items-center">
        <PlaceSearch
          query={query}
          onQueryChange={setQuery}
          onSelect={(r) =>
            onCenter({
              label: r.label,
              latitude: r.latitude,
              longitude: r.longitude,
            })
          }
          proximity={proximity}
          ariaLabel="Search area"
          placeholder="Search a place to explore reports around"
        />
        <Button
          type="button"
          variant="secondary"
          className="h-10"
          disabled={geo.status === "locating"}
          onClick={() =>
            geo.locate((found) => {
              setQuery("My location");
              onCenter({ label: "My location", ...found });
            })
          }
        >
          <LocateFixedIcon />{" "}
          {geo.status === "locating" ? "Locating…" : "Use my location"}
        </Button>
        <Select
          items={RADIUS_OPTIONS}
          value={String(radiusMeters)}
          onValueChange={(v) => v !== null && onRadius(Number(v))}
        >
          <SelectTrigger
            className="h-10 w-full border-transparent bg-muted"
            aria-label="Search radius"
          >
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {RADIUS_OPTIONS.map((o) => (
              <SelectItem key={o.value} value={o.value}>
                Within {o.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      {problem ? (
        <p role="alert" className="text-xs text-destructive">
          {problem}
        </p>
      ) : null}
    </div>
  );
}
