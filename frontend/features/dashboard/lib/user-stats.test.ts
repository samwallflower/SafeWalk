import { describe, expect, it } from "vitest";

import {
  reportStats,
  reportsByCategory,
  sessionStats,
  walksPerDay,
} from "./user-stats";

describe("sessionStats", () => {
  it("counts statuses and the completed percentage", () => {
    const stats = sessionStats([
      { status: "COMPLETED" },
      { status: "COMPLETED" },
      { status: "EMERGENCY" },
      { status: "ABANDONED" },
    ]);
    expect(stats).toEqual({
      total: 4,
      completed: 2,
      emergencies: 1,
      completedPercent: 50,
    });
  });
  it("handles no sessions", () => {
    expect(sessionStats([])).toEqual({
      total: 0,
      completed: 0,
      emergencies: 0,
      completedPercent: 0,
    });
  });
});

describe("reportStats", () => {
  it("counts by status and sums votes", () => {
    const stats = reportStats([
      { status: "ACTIVE", upvotes: 3, downvotes: 1 },
      { status: "HIDDEN", upvotes: 0, downvotes: 6 },
      { status: "UNDER_REVIEW", upvotes: 2, downvotes: 0 },
    ]);
    expect(stats).toEqual({
      total: 3,
      active: 1,
      hidden: 1,
      underReview: 1,
      upvotes: 5,
      downvotes: 7,
    });
  });
});

describe("walksPerDay", () => {
  const now = new Date(2026, 9, 11, 15);
  it("gives one entry per day with today last", () => {
    const days = walksPerDay(
      [{ status: "COMPLETED", startTime: "2026-10-11T09:00:00" }],
      7,
      now,
    );
    expect(days).toHaveLength(7);
    expect(days[6]).toEqual({ label: "Sun", value: 1 });
    expect(days[0].value).toBe(0);
  });

  it("counts several walks on a day but skips abandoned and older ones", () => {
    const days = walksPerDay(
      [
        { status: "COMPLETED", startTime: "2026-10-10T08:00:00" },
        { status: "EMERGENCY", startTime: "2026-10-10T18:00:00" },
        { status: "ABANDONED", startTime: "2026-10-10T19:00:00" },
        { status: "COMPLETED", startTime: "2026-09-01T09:00:00" },
      ],
      7,
      now,
    );
    expect(days[5].value).toBe(2);
    expect(days.reduce((s, d) => s + d.value, 0)).toBe(2);
  });
});

describe("reportsByCategory", () => {
  it("counts per category, largest first", () => {
    const category = (name: string) => ({
      category: { id: 1, name, severityWeight: 1, description: null },
    });
    const items = reportsByCategory([
      category("Theft"),
      category("Lighting"),
      category("Theft"),
    ]);
    expect(items).toEqual([
      { label: "Theft", value: 2 },
      { label: "Lighting", value: 1 },
    ]);
  });
});
