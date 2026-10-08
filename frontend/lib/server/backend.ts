import "server-only";

const DEFAULT_BACKEND_URL = "http://localhost:8080";
const DEFAULT_API_PREFIX = "/api/v1";

/** Absolute backend URL for an API path such as `/auth/login`. */
export function backendUrl(path: string): string {
  const base = (process.env.BACKEND_URL || DEFAULT_BACKEND_URL).replace(/\/+$/, "");
  const prefix = process.env.API_PREFIX || DEFAULT_API_PREFIX;
  return `${base}${prefix}${path}`;
}
