import { describe, expect, it } from "vitest";

import { reportStats, sessionStats } from "./user-stats";

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
