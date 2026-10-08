import type { Incident } from "@/features/incidents/types";

import type { MapFilterValues, TimeRange } from "../types";

const RANGE_MS: Record<Exclude<TimeRange, "all">, number> = {
  "24h": 24 * 3_600_000,
  "7d": 7 * 24 * 3_600_000,
  "30d": 30 * 24 * 3_600_000,
};

export function filtersActive(filters: MapFilterValues): boolean {
  return filters.categoryId !== null || filters.range !== "all";
}

/** Timestamps are zone-less; they are compared as local time, same as they are displayed. */
export function filterIncidents(
  incidents: readonly Incident[],
  filters: MapFilterValues,
  nowMs: number = Date.now(),
): Incident[] {
  return incidents.filter((i) => {
    if (filters.categoryId !== null && i.category.id !== filters.categoryId) return false;
    if (filters.range !== "all") {
      const age = nowMs - new Date(i.timestamp).getTime();
      if (age > RANGE_MS[filters.range]) return false;
    }
    return true;
  });
}
