"use client";

import { ArrowDownUpIcon, LocateFixedIcon, RouteIcon } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { PlaceSearch } from "@/features/places/components/PlaceSearch";
import type { Place } from "@/features/places/types";
import type { LatLng } from "@/lib/geo/haversine";
import type { GeolocationStatus } from "@/hooks/useGeolocation";

interface PlanFormProps {
  originQuery: string;
  destinationQuery: string;
  onOriginQuery: (query: string) => void;
  onDestinationQuery: (query: string) => void;
  origin: Place | null;
  onOrigin: (place: Place) => void;
  onDestination: (place: Place) => void;
  onUseMyLocation: () => void;
  onSwap: () => void;
  geoStatus: GeolocationStatus;
  proximity: LatLng;
  canSubmit: boolean;
  isPending: boolean;
  onSubmit: () => void;
}

const GEO_PROBLEMS: Partial<Record<GeolocationStatus, string>> = {
  denied: "Location permission denied. Search for your start instead.",
  unavailable: "Location is not available on this device.",
  error: "Could not get your location.",
};

export function PlanForm(props: PlanFormProps) {
  const problem = GEO_PROBLEMS[props.geoStatus];
  return (
    <form
      className="space-y-4 rounded-2xl bg-card p-4 shadow-sm ring-1 ring-foreground/5"
      onSubmit={(e) => {
        e.preventDefault();
        props.onSubmit();
      }}
    >
      <div className="space-y-1.5">
        <div className="flex items-center justify-between">
          <Label>From</Label>
          <Button
            type="button"
            variant="ghost"
            size="xs"
            disabled={props.geoStatus === "locating"}
            onClick={props.onUseMyLocation}
          >
            <LocateFixedIcon /> {props.geoStatus === "locating" ? "Locating…" : "Use my location"}
          </Button>
        </div>
        <PlaceSearch
          query={props.originQuery}
          onQueryChange={props.onOriginQuery}
          onSelect={(r) => props.onOrigin({ label: r.label, latitude: r.latitude, longitude: r.longitude })}
          proximity={props.proximity}
          ariaLabel="Start of route"
          placeholder="Search a start point"
        />
        {problem ? (
          <p role="alert" className="text-xs text-destructive">
            {problem}
          </p>
        ) : null}
      </div>

      <div className="flex justify-center">
        <Button type="button" variant="ghost" size="icon-sm" aria-label="Swap start and destination" onClick={props.onSwap}>
          <ArrowDownUpIcon />
        </Button>
      </div>

      <div className="space-y-1.5">
        <Label>To</Label>
        <PlaceSearch
          query={props.destinationQuery}
          onQueryChange={props.onDestinationQuery}
          onSelect={(r) => props.onDestination({ label: r.label, latitude: r.latitude, longitude: r.longitude })}
          proximity={props.origin ?? props.proximity}
          ariaLabel="Destination"
          placeholder="Search a destination"
        />
      </div>

      <Button type="submit" size="lg" className="h-11 w-full font-semibold" disabled={!props.canSubmit || props.isPending}>
        <RouteIcon /> {props.isPending ? "Finding routes…" : "Find safest route"}
      </Button>
    </form>
  );
}
