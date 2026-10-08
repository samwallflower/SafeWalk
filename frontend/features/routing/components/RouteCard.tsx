"use client";

import { FootprintsIcon } from "lucide-react";

import { Chip } from "@/components/shared/Chip";
import { estimateWalkingMinutes, formatDistance, formatDistanceDelta } from "@/lib/format";
import { cn } from "@/lib/utils";

import type { DecodedRoute } from "../lib/route-geometry";

interface RouteCardProps {
  item: DecodedRoute;
  recommended: DecodedRoute;
  /** Highest and lowest safety penalty among the options, used to scale and label the risk bar. */
  maxPenalty: number;
  minPenalty: number;
  selected: boolean;
  onSelect: () => void;
  onHover: (hovering: boolean) => void;
}

export function RouteCard({ item, recommended, maxPenalty, minPenalty, selected, onSelect, onHover }: RouteCardProps) {
  const { route, color } = item;
  const isRecommended = route.rank === 1;
  const extraDistance = route.actualDistanceMeters - recommended.route.actualDistanceMeters;
  const safe = route.safetyPenaltyMeters <= 0;
  const riskWidth = safe || maxPenalty <= 0 ? 4 : Math.max(8, Math.round((route.safetyPenaltyMeters / maxPenalty) * 100));
  const riskLabel = safe
    ? "None nearby"
    : route.safetyPenaltyMeters === minPenalty
      ? "Lowest of these routes"
      : `+${Math.round(((route.safetyPenaltyMeters - minPenalty) / Math.max(minPenalty, 1)) * 100)}% vs lowest`;

  return (
    <button
      type="button"
      onClick={onSelect}
      onMouseEnter={() => onHover(true)}
      onMouseLeave={() => onHover(false)}
      onFocus={() => onHover(true)}
      onBlur={() => onHover(false)}
      aria-pressed={selected}
      className={cn(
        "w-full space-y-3 rounded-2xl bg-card p-4 text-left shadow-sm ring-1 ring-foreground/5 transition",
        selected && "ring-2 ring-primary",
      )}
    >
      <div className="flex items-center justify-between gap-2">
        <span className="flex items-center gap-2 font-bold">
          <span className="size-3 rounded-full" style={{ backgroundColor: color }} aria-hidden="true" />
          Route {route.rank}
        </span>
        {isRecommended ? <Chip tone="success">Recommended</Chip> : <Chip>Alternative</Chip>}
      </div>

      <div className="flex items-end justify-between gap-3">
        <div>
          <p className="text-2xl font-bold">{formatDistance(route.actualDistanceMeters)}</p>
          <p className="flex items-center gap-1 text-xs text-muted-foreground">
            <FootprintsIcon className="size-3.5" aria-hidden="true" />
            about {estimateWalkingMinutes(route.actualDistanceMeters)} min walk (estimate)
          </p>
        </div>
        {!isRecommended ? (
          <p className="text-xs font-semibold text-muted-foreground">{formatDistanceDelta(extraDistance)} vs Route 1</p>
        ) : null}
      </div>

      <div className="space-y-1.5">
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted-foreground">Incident risk</span>
          <span className={cn("font-semibold", safe ? "text-success" : route.safetyPenaltyMeters === minPenalty ? "text-foreground" : "text-destructive")}>
            {riskLabel}
          </span>
        </div>
        <div className="h-2 overflow-hidden rounded-full bg-muted" role="img" aria-label={`Incident risk: ${riskLabel}`}>
          <div className="h-full rounded-full" style={{ width: `${riskWidth}%`, backgroundColor: color }} />
        </div>
      </div>
    </button>
  );
}
