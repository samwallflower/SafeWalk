"use client";

import { Popup } from "react-map-gl/mapbox";

import { CategoryBadge } from "@/features/incidents/components/CategoryBadge";
import { IncidentSummary } from "@/features/incidents/components/IncidentSummary";
import type { Incident } from "@/features/incidents/types";
import { formatRelative } from "@/lib/format";

interface IncidentPopupProps {
  incident: Incident;
  /** `hover` shows a compact tooltip, `selected` the full card. */
  variant: "hover" | "selected";
  onClose: () => void;
}

export function IncidentPopup({ incident, variant, onClose }: IncidentPopupProps) {
  const selected = variant === "selected";
  return (
    <Popup
      latitude={incident.latitude}
      longitude={incident.longitude}
      anchor="bottom"
      offset={selected ? 18 : 14}
      closeButton={selected}
      closeOnClick={false}
      focusAfterOpen={false}
      onClose={onClose}
      maxWidth="320px"
    >
      {selected ? (
        <IncidentSummary incident={incident} />
      ) : (
        <div className="flex items-center gap-2 text-xs">
          <CategoryBadge name={incident.category.name} />
          <span className="text-muted-foreground">{formatRelative(incident.timestamp)}</span>
        </div>
      )}
    </Popup>
  );
}
