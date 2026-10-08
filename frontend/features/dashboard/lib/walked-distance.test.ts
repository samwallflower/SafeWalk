import { describe, expect, it } from "vitest";

import {
  completedRouteIds,
  MAX_WALKS_COUNTED,
  totalWalkedMeters,
} from "./walked-distance";

const session = (
  routeId: number | null,
  status: "COMPLETED" | "ACTIVE" | "EMERGENCY" | "ABANDONED",
  day = 1,
) => ({
  routeId,
  status,
  startTime: `2026-10-${String(day).padStart(2, "0")}T10:00:00`,
});

describe("completedRouteIds", () => {
  it("keeps only completed walks that have a route", () => {
    const { routeIds } = completedRouteIds([
      session(1, "COMPLETED"),
      session(2, "ACTIVE"),
      session(null, "COMPLETED"),
      session(3, "EMERGENCY"),
    ]);
    expect(routeIds).toEqual([1]);
  });

  it("orders newest first and caps the lookups", () => {
    const many = Array.from({ length: MAX_WALKS_COUNTED + 5 }, (_, i) =>
      session(i + 1, "COMPLETED", (i % 28) + 1),
    );
    const result = completedRouteIds(many);
    expect(result.routeIds).toHaveLength(MAX_WALKS_COUNTED);
    expect(result.capped).toBe(true);
  });
});

describe("totalWalkedMeters", () => {
  it("adds each walk's route length, counting a repeated route twice", () => {
    const routes = new Map([
      [1, { actualDistanceMeters: 1200 }],
      [2, { actualDistanceMeters: 800 }],
    ]);
    expect(totalWalkedMeters([1, 2, 1], routes)).toBe(3200);
  });

  it("ignores routes that have not loaded yet", () => {
    expect(totalWalkedMeters([9], new Map())).toBe(0);
  });
});
