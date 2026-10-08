import { describe, expect, it } from "vitest";

import { contactSchema, toContactBody } from "./contact-schema";

const valid = { contactName: "Marcus", contactEmail: "m@x.com", contactPhone: "" };

describe("contactSchema", () => {
  it("accepts a contact without a phone", () => expect(contactSchema.safeParse(valid).success).toBe(true));
  it("accepts an E.164 phone", () => expect(contactSchema.safeParse({ ...valid, contactPhone: "+36301234567" }).success).toBe(true));
  it("rejects a non-E.164 phone", () => expect(contactSchema.safeParse({ ...valid, contactPhone: "0630123" }).success).toBe(false));
  it("requires name and a valid email", () => {
    expect(contactSchema.safeParse({ ...valid, contactName: " " }).success).toBe(false);
    expect(contactSchema.safeParse({ ...valid, contactEmail: "nope" }).success).toBe(false);
  });
  it("omits a blank phone from the request body", () => {
    expect(toContactBody(valid)).toEqual({ contactName: "Marcus", contactEmail: "m@x.com" });
  });
});
