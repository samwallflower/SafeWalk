import { describe, expect, it } from "vitest";

import { decodePolyline } from "./polyline";

describe("decodePolyline", () => {
  it("decodes the reference example from Google's docs", () => {
    const points = decodePolyline("_p~iF~ps|U_ulLnnqC_mqNvxq`@");
    expect(points).toHaveLength(3);
    expect(points[0]).toEqual({ latitude: 38.5, longitude: -120.2 });
    expect(points[1]).toEqual({ latitude: 40.7, longitude: -120.95 });
    expect(points[2]).toEqual({ latitude: 43.252, longitude: -126.453 });
  });

  it("returns an empty list for an empty string", () => {
    expect(decodePolyline("")).toEqual([]);
  });
});
