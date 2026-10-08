import { ApiError } from "@/lib/http/api-error";
import { request } from "@/lib/http/request";
import type { SessionUser } from "@/lib/auth/session-types";

import type { LoginValues } from "../schemas/login-schema";
import type { RegisterValues } from "../schemas/register-schema";
import type { VerifyValues } from "../schemas/verify-schema";
import type { UserDto } from "../types";

/** Browser calls: login/logout/session hit our own BFF routes, never the backend directly. */
async function bff<T>(path: string, method: "GET" | "POST", body?: unknown): Promise<T> {
  const response = await fetch(`/api/auth${path}`, {
    method,
    headers: body === undefined ? undefined : { "Content-Type": "application/json" },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const json: unknown = await response.json().catch(() => null);
  const record = typeof json === "object" && json !== null ? (json as { message?: unknown; data?: unknown }) : {};
  if (!response.ok) {
    throw new ApiError(response.status, typeof record.message === "string" ? record.message : "Request failed");
  }
  return record.data as T;
}

export const authApi = {
  session: () => bff<SessionUser | null>("/session", "GET"),
  login: (values: LoginValues) => bff<SessionUser>("/login", "POST", values),
  logout: () => bff<null>("/logout", "POST"),
  register: (values: RegisterValues) => request<UserDto>("/users/register", { method: "POST", body: values }),
  verify: (values: VerifyValues) => request<null>("/auth/verify", { method: "POST", body: values }),
  resendVerification: (email: string) =>
    request<null>("/auth/resend-verification", { method: "POST", body: { email } }),
};
