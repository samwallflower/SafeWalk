import { isAdmin, type SessionUser } from "@/lib/auth/session-types";

export type GuardDecision = { type: "next" } | { type: "redirect"; to: string };

const AUTH_REQUIRED = ["/dashboard", "/report", "/admin"];
const GUEST_ONLY = ["/login", "/register"];

function matches(pathname: string, prefix: string): boolean {
  return pathname === prefix || pathname.startsWith(`${prefix}/`);
}

export function homeFor(user: SessionUser): string {
  return isAdmin(user) ? "/admin" : "/dashboard";
}

/** Routing-only access decision. The backend still enforces real authorization. */
export function decideAccess(pathname: string, search: string, user: SessionUser | null): GuardDecision {
  if (AUTH_REQUIRED.some((p) => matches(pathname, p))) {
    if (!user) {
      return { type: "redirect", to: `/login?redirect=${encodeURIComponent(pathname + search)}` };
    }
    if (matches(pathname, "/admin") && !isAdmin(user)) {
      return { type: "redirect", to: "/forbidden" };
    }
  }
  if (user && GUEST_ONLY.some((p) => matches(pathname, p))) {
    return { type: "redirect", to: homeFor(user) };
  }
  return { type: "next" };
}
