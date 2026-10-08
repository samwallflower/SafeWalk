import type { RouteDto } from "@/features/routing/types";
import type { WalkSession } from "@/features/sessions/types";

/** Each completed walk's route is one request, so only the most recent walks are looked up. */
export const MAX_WALKS_COUNTED = 50;

export interface WalkedRoutes {
  routeIds: number[];
  /** True when there were more completed walks than `MAX_WALKS_COUNTED`. */
  capped: boolean;
}

/** Distinct route ids of the user's completed walks, newest first. */
export function completedRouteIds(
  sessions: readonly Pick<WalkSession, "status" | "routeId" | "startTime">[],
): WalkedRoutes {
  const completed = sessions
    .filter((s) => s.status === "COMPLETED" && s.routeId !== null)
    .sort((a, b) => b.startTime.localeCompare(a.startTime));
  const ids = completed.map((s) => s.routeId as number);
  const recent = ids.slice(0, MAX_WALKS_COUNTED);
  return { routeIds: recent, capped: ids.length > MAX_WALKS_COUNTED };
}

/** Sum of route lengths; each completed walk counts once, even if two walks used the same route. */
export function totalWalkedMeters(
  routeIds: readonly number[],
  routes: ReadonlyMap<number, Pick<RouteDto, "actualDistanceMeters">>,
): number {
  return routeIds.reduce(
    (sum, id) => sum + (routes.get(id)?.actualDistanceMeters ?? 0),
    0,
  );
}
