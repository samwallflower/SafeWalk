import { describe, expect, it } from "vitest";

import { decodeToken, isExpired } from "@/lib/auth/jwt";
import type { SessionUser } from "@/lib/auth/session-types";

import { decideAccess } from "./route-guard";
import { safeRedirect } from "./safe-redirect";

function makeToken(payload: Record<string, unknown>): string {
  const b64 = (o: unknown) => Buffer.from(JSON.stringify(o)).toString("base64url");
  return `${b64({ alg: "HS256" })}.${b64(payload)}.sig`;
}

const user: SessionUser = { id: 1, email: "u@x.com", roles: ["ROLE_USER"] };
const admin: SessionUser = { id: 2, email: "a@x.com", roles: ["ROLE_ADMIN"] };

describe("decodeToken", () => {
  it("reads id, email, roles and exp", () => {
    const decoded = decodeToken(makeToken({ id: 7, sub: "a@b.com", roles: ["ROLE_ADMIN"], exp: 2_000_000_000 }));
    expect(decoded?.user).toEqual({ id: 7, email: "a@b.com", roles: ["ROLE_ADMIN"] });
    expect(decoded?.exp).toBe(2_000_000_000);
  });

  it("returns null for garbage or incomplete tokens", () => {
    expect(decodeToken("not-a-jwt")).toBeNull();
    expect(decodeToken(makeToken({ sub: "a@b.com" }))).toBeNull();
  });

  it("detects expiry", () => {
    const decoded = decodeToken(makeToken({ id: 1, sub: "a", roles: [], exp: 100 }));
    expect(decoded && isExpired(decoded, 101_000)).toBe(true);
    expect(decoded && isExpired(decoded, 99_000)).toBe(false);
  });
});

describe("decideAccess", () => {
  it("sends guests to login keeping the destination", () => {
    expect(decideAccess("/dashboard/reports", "?x=1", null)).toEqual({
      type: "redirect",
      to: "/login?redirect=%2Fdashboard%2Freports%3Fx%3D1",
    });
  });

  it("blocks non-admins from /admin", () => {
    expect(decideAccess("/admin/users", "", user)).toEqual({ type: "redirect", to: "/forbidden" });
  });

  it("lets admins into /admin", () => {
    expect(decideAccess("/admin", "", admin)).toEqual({ type: "next" });
  });

  it("keeps signed-in users off the login page", () => {
    expect(decideAccess("/login", "", user)).toEqual({ type: "redirect", to: "/dashboard" });
    expect(decideAccess("/login", "", admin)).toEqual({ type: "redirect", to: "/admin" });
  });

  it("leaves public pages alone", () => {
    expect(decideAccess("/map", "", null)).toEqual({ type: "next" });
  });
});

describe("safeRedirect", () => {
  it("accepts relative paths only", () => {
    expect(safeRedirect("/dashboard?a=1")).toBe("/dashboard?a=1");
    expect(safeRedirect("//evil.com")).toBeNull();
    expect(safeRedirect("https://evil.com")).toBeNull();
    expect(safeRedirect("/\\evil.com")).toBeNull();
    expect(safeRedirect(null)).toBeNull();
  });
});
