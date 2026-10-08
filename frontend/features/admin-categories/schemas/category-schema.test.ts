import { describe, expect, it } from "vitest";

import { categorySchema } from "./category-schema";

describe("categorySchema", () => {
  const valid = { name: "Robbery", severityWeight: 20, description: "" };
  it("accepts 1..20", () => {
    expect(categorySchema.safeParse(valid).success).toBe(true);
    expect(categorySchema.safeParse({ ...valid, severityWeight: 1 }).success).toBe(true);
  });
  it("rejects out of range and non-integers", () => {
    expect(categorySchema.safeParse({ ...valid, severityWeight: 0 }).success).toBe(false);
    expect(categorySchema.safeParse({ ...valid, severityWeight: 21 }).success).toBe(false);
    expect(categorySchema.safeParse({ ...valid, severityWeight: 2.5 }).success).toBe(false);
  });
  it("requires a name", () => expect(categorySchema.safeParse({ ...valid, name: " " }).success).toBe(false));
});
