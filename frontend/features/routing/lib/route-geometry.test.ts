import { describe, expect, it } from "vitest";

import type { RouteDto } from "../types";
import { boundsOf, coveringCircle, decodeRoutes, routeColor, routesToGeoJson, RECOMMENDED_COLOR } from "./route-geometry";

function route(id: number, rank: number, polyline = "_p~iF~ps|U_ulLnnqC"): RouteDto {
  return {
    id,
    polyline,
    actualDistanceMeters: 1000,
    safetyPenaltyMeters: 0,
    virtualDistanceMeters: 1000,
    rank,
    routeRequestId: "r",
  };
}

describe("route geometry", () => {
  it("colors the recommended route distinctly", () => {
    expect(routeColor(1)).toBe(RECOMMENDED_COLOR);
    expect(routeColor(2)).not.toBe(RECOMMENDED_COLOR);
  });

  it("sorts by rank and decodes", () => {
    const decoded = decodeRoutes([route(2, 2), route(1, 1)]);
    expect(decoded.map((d) => d.route.id)).toEqual([1, 2]);
    expect(decoded[0].points).toHaveLength(2);
  });

  it("emits active routes last, in [lng, lat] order", () => {
    const decoded = decodeRoutes([route(1, 1), route(2, 2)]);
    const geo = routesToGeoJson(decoded, new Set([1]));
    expect(geo.features.map((f) => f.properties.routeId)).toEqual([2, 1]);
    expect(geo.features[1].geometry.coordinates[0]).toEqual([-120.2, 38.5]);
  });

  it("computes bounds and a covering circle", () => {
    const points = [
      { latitude: 52.9, longitude: -1.2 },
      { latitude: 53.0, longitude: -1.1 },
    ];
    expect(boundsOf(points)).toEqual({ southWest: [-1.2, 52.9], northEast: [-1.1, 53.0] });
    const circle = coveringCircle(points);
    expect(circle?.radiusMeters).toBeGreaterThan(5000);
    expect(boundsOf([])).toBeNull();
  });
});
