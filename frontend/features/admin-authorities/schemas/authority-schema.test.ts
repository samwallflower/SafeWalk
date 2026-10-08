import { describe, expect, it } from "vitest";

import { authoritySchema, toAuthorityBody } from "./authority-schema";

const valid = { countryCode: "HU", countryName: "Hungary", policeNumber: "107", ambulanceNumber: "104", generalEmergencyNumber: "112" };

describe("authoritySchema", () => {
  it("accepts a valid authority", () => expect(authoritySchema.safeParse(valid).success).toBe(true));
  it("requires an uppercase 2-letter code", () => {
    expect(authoritySchema.safeParse({ ...valid, countryCode: "hu" }).success).toBe(false);
    expect(authoritySchema.safeParse({ ...valid, countryCode: "HUN" }).success).toBe(false);
  });
  it("requires digit-only numbers, general optional", () => {
    expect(authoritySchema.safeParse({ ...valid, policeNumber: "1-07" }).success).toBe(false);
    expect(authoritySchema.safeParse({ ...valid, generalEmergencyNumber: "" }).success).toBe(true);
  });
  it("omits a blank general number from the body", () => {
    expect(toAuthorityBody({ ...valid, generalEmergencyNumber: "" })).not.toHaveProperty("generalEmergencyNumber");
  });
});
