import { ApiError } from "@/lib/http/api-error";

import { authApi } from "./api/auth-api";

let handling = false;

/**
 * On a 401 from a non-auth call: clear the cookie, then go to login keeping the destination.
 * The cookie must be cleared first, otherwise the guest-only rule bounces /login back.
 */
export function handleUnauthorized(error: unknown): void {
  if (!(error instanceof ApiError) || error.status !== 401 || handling) return;
  handling = true;
  const destination = window.location.pathname + window.location.search;
  void authApi
    .logout()
    .catch(() => null)
    .finally(() => {
      const login = new URL("/login", window.location.origin);
      login.searchParams.set("redirect", destination);
      login.searchParams.set("expired", "1");
      window.location.assign(login);
    });
}
