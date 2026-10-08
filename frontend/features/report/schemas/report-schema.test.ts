import { describe, expect, it } from "vitest";

import { reportSchema } from "./report-schema";

const valid = { categoryId: 1, description: "Broken lamp", latitude: 52.9, longitude: -1.1, isAnonymous: true };

describe("reportSchema", () => {
  it("accepts a valid report", () => expect(reportSchema.safeParse(valid).success).toBe(true));

  it("requires a category with a friendly message", () => {
    const result = reportSchema.safeParse({ ...valid, categoryId: undefined });
    expect(result.success).toBe(false);
    expect(result.error?.issues[0].message).toBe("Select a category");
  });

  it("trims and requires a description, max 280", () => {
    expect(reportSchema.safeParse({ ...valid, description: "   " }).success).toBe(false);
    expect(reportSchema.safeParse({ ...valid, description: "x".repeat(281) }).success).toBe(false);
    expect(reportSchema.safeParse({ ...valid, description: "x".repeat(280) }).success).toBe(true);
  });

  it("enforces coordinate ranges", () => {
    expect(reportSchema.safeParse({ ...valid, latitude: 91 }).success).toBe(false);
    expect(reportSchema.safeParse({ ...valid, longitude: -181 }).success).toBe(false);
  });
});
