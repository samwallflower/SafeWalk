"use client";

import { Button } from "@/components/ui/button";

import type { GeolocationStatus } from "@/hooks/useGeolocation";

const PROBLEMS: Partial<Record<GeolocationStatus, string>> = {
  denied: "Permission denied. Search for a place instead.",
  unavailable: "Not available on this device.",
  error: "Could not get your location.",
};

interface LocationCardProps {
  status: GeolocationStatus;
  hasPosition: boolean;
  address: string | null | undefined;
  onLocate: () => void;
  onRecenter: () => void;
}

export function LocationCard({ status, hasPosition, address, onLocate, onRecenter }: LocationCardProps) {
  const problem = PROBLEMS[status];
  const locating = status === "locating";
  return (
    <div className="flex items-center gap-3 rounded-xl bg-muted px-3 py-2">
      <span
        className={`size-2.5 shrink-0 rounded-full ${hasPosition ? "bg-primary" : "bg-muted-foreground/50"}`}
        aria-hidden="true"
      />
      <div className="min-w-0 flex-1">
        <p className="text-[10px] font-semibold tracking-wider text-muted-foreground uppercase">
          {hasPosition ? "Locating you" : "Your location"}
        </p>
        <p className={`truncate text-sm font-medium ${problem ? "text-destructive" : ""}`} role={problem ? "alert" : undefined}>
          {problem ?? (hasPosition ? (address ?? "Current position") : "Not shared yet")}
        </p>
      </div>
      <Button
        type="button"
        variant="secondary"
        size="sm"
        className="bg-card"
        disabled={locating}
        onClick={hasPosition ? onRecenter : onLocate}
      >
        {locating ? "Locating…" : hasPosition ? "Recenter" : "Use my location"}
      </Button>
    </div>
  );
}
