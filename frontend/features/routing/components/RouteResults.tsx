"use client";

import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { Skeleton } from "@/components/ui/skeleton";

import type { DecodedRoute } from "../lib/route-geometry";
import { RouteCard } from "./RouteCard";

interface RouteResultsProps {
  status: "idle" | "pending" | "error" | "success";
  error: Error | null;
  routes: readonly DecodedRoute[];
  selectedId: number | null;
  onSelect: (id: number) => void;
  onHover: (id: number | null) => void;
  onRetry: () => void;
}

export function RouteResults({ status, error, routes, selectedId, onSelect, onHover, onRetry }: RouteResultsProps) {
  if (status === "idle") {
    return <EmptyState title="Choose where you are going" description="Pick a start and a destination to compare walking routes." />;
  }
  if (status === "pending") {
    return (
      <div className="space-y-3" role="status" aria-label="Finding routes">
        {Array.from({ length: 2 }, (_, i) => (
          <Skeleton key={i} className="h-40 rounded-2xl" />
        ))}
      </div>
    );
  }
  if (status === "error") {
    return (
      <ErrorState
        title="Could not find routes"
        message={error?.message.replace(/^Error:\s*/, "") ?? "Routing is unavailable right now."}
        onRetry={onRetry}
      />
    );
  }
  if (routes.length === 0) {
    return <EmptyState title="No walking route found" description="Try a different start or destination." />;
  }
  const penalties = routes.map((r) => r.route.safetyPenaltyMeters);
  const maxPenalty = Math.max(...penalties);
  const minPenalty = Math.min(...penalties);
  return (
    <ul className="space-y-3" aria-label="Route options">
      {routes.map((item) => (
        <li key={item.route.id}>
          <RouteCard
            item={item}
            recommended={routes[0]}
            maxPenalty={maxPenalty}
            minPenalty={minPenalty}
            selected={item.route.id === selectedId}
            onSelect={() => onSelect(item.route.id)}
            onHover={(hovering) => onHover(hovering ? item.route.id : null)}
          />
        </li>
      ))}
    </ul>
  );
}
