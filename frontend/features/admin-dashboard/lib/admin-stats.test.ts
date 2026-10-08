import { describe, expect, it } from "vitest";

import {
  countByCategory,
  countPerDay,
  countSessionsByStatus,
  emergencySummary,
} from "./admin-stats";

const cat = (name: string) => ({
  category: { id: 1, name, severityWeight: 1, description: null },
});

describe("admin stats", () => {
  it("counts by category, largest first", () => {
    expect(countByCategory([cat("Theft"), cat("Fire"), cat("Theft")])).toEqual([
      { label: "Theft", value: 2 },
      { label: "Fire", value: 1 },
    ]);
  });

  it("counts per day and fills gaps with zero", () => {
    const result = countPerDay([
      { timestamp: "2026-10-01T10:00:00" },
      { timestamp: "2026-10-01T12:00:00" },
      { timestamp: "2026-10-04T09:00:00" },
    ]);
    expect(result).toEqual([
      { label: "10-01", value: 2 },
      { label: "10-02", value: 0 },
      { label: "10-03", value: 0 },
      { label: "10-04", value: 1 },
    ]);
    expect(countPerDay([])).toEqual([]);
  });

  it("counts sessions by status", () => {
    expect(
      countSessionsByStatus([
        { status: "COMPLETED" },
        { status: "COMPLETED" },
        { status: "EMERGENCY" },
      ]),
    ).toEqual({
      ACTIVE: 0,
      COMPLETED: 2,
      EMERGENCY: 1,
      ABANDONED: 0,
    });
  });

  it("summarises emergencies", () => {
    expect(
      emergencySummary([
        { resolved: true },
        { resolved: false },
        { resolved: false },
      ]),
    ).toEqual({ total: 3, resolved: 1, unresolved: 2 });
  });
});
