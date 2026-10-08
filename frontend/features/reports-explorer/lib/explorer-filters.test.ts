import { describe, expect, it } from "vitest";

import type { Incident } from "@/features/incidents/types";

import {
  DEFAULT_FILTERS,
  filterReports,
  filtersAreDefault,
  paginate,
  sortReports,
} from "./explorer-filters";

const NOW = new Date("2026-10-05T12:00:00").getTime();

function report(
  id: number,
  categoryId: number,
  timestamp: string,
  up = 0,
  down = 0,
  description = "x",
): Incident {
  return {
    id,
    isAnonymous: true,
    reporterName: null,
    description,
    latitude: 1,
    longitude: 1,
    timestamp,
    upvotes: up,
    downvotes: down,
    category: {
      id: categoryId,
      name: categoryId === 1 ? "Robbery" : "Fire",
      severityWeight: 1,
      description: null,
    },
    status: "ACTIVE",
  };
}

const list = [
  report(1, 1, "2026-10-05T10:00:00", 5, 0, "phone stolen near station"),
  report(2, 2, "2026-10-03T10:00:00", 1, 4, "bin fire"),
  report(3, 1, "2026-08-01T10:00:00", 9, 1, "bag snatched"),
];

describe("filterReports", () => {
  it("combines category, time and text", () => {
    expect(
      filterReports(list, { ...DEFAULT_FILTERS, categoryId: 1 }, NOW).map(
        (r) => r.id,
      ),
    ).toEqual([1, 3]);
    expect(
      filterReports(list, { ...DEFAULT_FILTERS, range: "7d" }, NOW).map(
        (r) => r.id,
      ),
    ).toEqual([1, 2]);
    expect(
      filterReports(list, { ...DEFAULT_FILTERS, query: "STATION" }, NOW).map(
        (r) => r.id,
      ),
    ).toEqual([1]);
    expect(
      filterReports(list, { ...DEFAULT_FILTERS, query: "fire" }, NOW).map(
        (r) => r.id,
      ),
    ).toEqual([2]);
  });
  it("matches a report id, with or without #INC-", () => {
    expect(
      filterReports(list, { ...DEFAULT_FILTERS, query: "#INC-3" }, NOW).map(
        (r) => r.id,
      ),
    ).toEqual([3]);
    expect(
      filterReports(list, { ...DEFAULT_FILTERS, query: "2" }, NOW).map(
        (r) => r.id,
      ),
    ).toEqual([2]);
  });
});

describe("sortReports", () => {
  it("sorts by date and votes without mutating the input", () => {
    expect(sortReports(list, "newest").map((r) => r.id)).toEqual([1, 2, 3]);
    expect(sortReports(list, "oldest").map((r) => r.id)).toEqual([3, 2, 1]);
    expect(sortReports(list, "most-upvoted").map((r) => r.id)).toEqual([
      3, 1, 2,
    ]);
    expect(sortReports(list, "most-downvoted").map((r) => r.id)).toEqual([
      2, 3, 1,
    ]);
    expect(list.map((r) => r.id)).toEqual([1, 2, 3]);
  });
});

describe("paginate", () => {
  const items = Array.from({ length: 53 }, (_, i) => i);
  it("slices pages and reports totals", () => {
    const page = paginate(items, 2, 25);
    expect(page.items[0]).toBe(25);
    expect(page.items).toHaveLength(25);
    expect(page.pageCount).toBe(3);
    expect(page.total).toBe(53);
  });
  it("clamps out-of-range pages and handles empty lists", () => {
    expect(paginate(items, 99, 25).page).toBe(3);
    expect(paginate(items, 0, 25).page).toBe(1);
    expect(paginate([], 1, 25)).toEqual({
      items: [],
      page: 1,
      pageCount: 1,
      total: 0,
    });
  });
});

describe("filtersAreDefault", () => {
  it("is false once anything is set", () => {
    expect(filtersAreDefault(DEFAULT_FILTERS)).toBe(true);
    expect(filtersAreDefault({ ...DEFAULT_FILTERS, query: " x " })).toBe(false);
    expect(filtersAreDefault({ ...DEFAULT_FILTERS, sort: "oldest" })).toBe(
      true,
    );
  });
});
