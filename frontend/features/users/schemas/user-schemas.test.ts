import { describe, expect, it } from "vitest";

import { createUserSchema, editUserSchema } from "./user-schemas";

describe("createUserSchema", () => {
  it("accepts a complete new user", () => {
    const result = createUserSchema.safeParse({ email: "a@b.co", password: "Passw0rdOK", firstName: "Ann", lastName: "Lee" });
    expect(result.success).toBe(true);
  });

  it("asks for a password with a letter and a number", () => {
    const result = createUserSchema.safeParse({ email: "a@b.co", password: "onlyletters", firstName: "Ann", lastName: "Lee" });
    expect(result.success).toBe(false);
  });
});

describe("editUserSchema", () => {
  it("lets the phone be left blank", () => {
    expect(editUserSchema.safeParse({ firstName: "Ann", lastName: "Lee", phoneNumber: "" }).success).toBe(true);
  });

  it("accepts an international number", () => {
    expect(editUserSchema.safeParse({ firstName: "Ann", lastName: "Lee", phoneNumber: "+36 30 123 4567" }).success).toBe(true);
  });

  it("rejects a phone that is not a phone number", () => {
    expect(editUserSchema.safeParse({ firstName: "Ann", lastName: "Lee", phoneNumber: "call me" }).success).toBe(false);
  });

  it("needs both names", () => {
    expect(editUserSchema.safeParse({ firstName: " ", lastName: "Lee", phoneNumber: "" }).success).toBe(false);
  });
});
