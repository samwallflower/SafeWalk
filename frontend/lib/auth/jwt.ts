import { isRecord } from "@/lib/http/envelope";

import type { SessionUser } from "./session-types";

export interface DecodedToken {
  user: SessionUser;
  /** Expiry in epoch seconds. */
  exp: number;
}

function decodeBase64Url(input: string): string {
  const base64 = input.replace(/-/g, "+").replace(/_/g, "/");
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
  const bytes = Uint8Array.from(atob(padded), (c) => c.charCodeAt(0));
  return new TextDecoder().decode(bytes);
}

/**
 * Reads claims from a backend JWT WITHOUT verifying the signature.
 * For routing/UI decisions only; the backend is the authority on every request.
 */
export function decodeToken(token: string): DecodedToken | null {
  try {
    const payload: unknown = JSON.parse(decodeBase64Url(token.split(".")[1] ?? ""));
    if (!isRecord(payload)) return null;
    const { id, sub, roles, exp } = payload;
    if (typeof id !== "number" || typeof sub !== "string" || typeof exp !== "number") return null;
    const roleList = Array.isArray(roles)
      ? roles.filter((r): r is string => typeof r === "string")
      : [];
    return { user: { id, email: sub, roles: roleList }, exp };
  } catch {
    return null;
  }
}

export function isExpired(decoded: DecodedToken, nowMs: number = Date.now()): boolean {
  return decoded.exp * 1000 <= nowMs;
}
