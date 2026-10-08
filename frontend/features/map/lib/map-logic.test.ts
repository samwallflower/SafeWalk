import { describe, expect, it } from "vitest";

import type { Incident } from "@/features/incidents/types";
import { haversineMeters } from "@/lib/geo/haversine";

import { circleContains, nextFetchArea, padCircle, viewportCircle } from "./area";
import { filterIncidents } from "./filter-incidents";
import { heatPointsToGeoJson, incidentsToGeoJson } from "./geojson";

const NOW = new Date("2026-10-05T12:00:00").getTime();

function incident(id: number, categoryId: number, timestamp: string): Incident {
  return {
    id,
    isAnonymous: true,
    reporterName: null,
    description: "x",
    latitude: 52.95,
    longitude: -1.15,
    timestamp,
    upvotes: 0,
    downvotes: 0,
    category: { id: categoryId, name: `c${categoryId}`, severityWeight: categoryId, description: null },
    status: "ACTIVE",
  };
}

describe("haversineMeters", () => {
  it("is ~111km per degree of latitude", () => {
    const d = haversineMeters({ latitude: 0, longitude: 0 }, { latitude: 1, longitude: 0 });
    expect(d).toBeGreaterThan(110_000);
    expect(d).toBeLessThan(112_000);
  });
});

describe("viewportCircle", () => {
  it("centers on the view and reaches the corner", () => {
    const c = viewportCircle({ west: -1.2, east: -1.1, south: 52.9, north: 53.0 });
    expect(c.latitude).toBeCloseTo(52.95);
    expect(c.longitude).toBeCloseTo(-1.15);
    expect(c.radiusMeters).toBeGreaterThan(5_000);
    expect(c.radiusMeters).toBeLessThan(10_000);
  });
});

describe("nextFetchArea", () => {
  const view = { latitude: 52.95, longitude: -1.15, radiusMeters: 2_000 };

  it("refuses to fetch when the view exceeds the cap", () => {
    expect(nextFetchArea(null, { ...view, radiusMeters: 70_000 }, 60_000)).toBeNull();
  });

  it("pads the first area", () => {
    const area = nextFetchArea(null, view, 60_000);
    expect(area?.radiusMeters).toBe(padCircle(view).radiusMeters);
  });

  it("keeps the previous area for a small pan (no refetch)", () => {
    const previous = nextFetchArea(null, view, 60_000);
    const panned = { ...view, longitude: view.longitude + 0.002 };
    expect(nextFetchArea(previous, panned, 60_000)).toBe(previous);
  });

  it("refetches when the view leaves the area", () => {
    const previous = nextFetchArea(null, view, 60_000);
    const far = { ...view, longitude: view.longitude + 0.5 };
    const next = nextFetchArea(previous, far, 60_000);
    expect(next).not.toBe(previous);
    expect(next && circleContains(next, far)).toBe(true);
  });

  it("refetches tighter after zooming far in", () => {
    const previous = nextFetchArea(null, { ...view, radiusMeters: 40_000 }, 60_000);
    const next = nextFetchArea(previous, { ...view, radiusMeters: 1_000 }, 60_000);
    expect(next).not.toBe(previous);
  });

  it("never exceeds the cap", () => {
    const area = nextFetchArea(null, { ...view, radiusMeters: 55_000 }, 60_000);
    expect(area?.radiusMeters).toBe(60_000);
  });
});

describe("filterIncidents", () => {
  const list = [
    incident(1, 1, "2026-10-05T10:00:00"),
    incident(2, 2, "2026-10-03T10:00:00"),
    incident(3, 1, "2026-08-01T10:00:00"),
  ];

  it("filters by category", () => {
    expect(filterIncidents(list, { categoryId: 1, range: "all" }, NOW).map((i) => i.id)).toEqual([1, 3]);
  });

  it("filters by time range", () => {
    expect(filterIncidents(list, { categoryId: null, range: "24h" }, NOW).map((i) => i.id)).toEqual([1]);
    expect(filterIncidents(list, { categoryId: null, range: "7d" }, NOW).map((i) => i.id)).toEqual([1, 2]);
  });
});

describe("geojson", () => {
  it("uses [lng, lat] order", () => {
    const fc = heatPointsToGeoJson([{ latitude: 52.9, longitude: -1.1, severityWeight: 3 }]);
    expect(fc.features[0].geometry.coordinates).toEqual([-1.1, 52.9]);
    const ic = incidentsToGeoJson([incident(1, 1, "2026-10-05T10:00:00")]);
    expect(ic.features[0].properties).toEqual({ id: 1, severityWeight: 1 });
  });
});
