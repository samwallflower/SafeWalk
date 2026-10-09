import { ApiError } from "./api-error";
import { isRecord } from "./envelope";

const PROXY_BASE = "/api/proxy";

interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "DELETE";
  body?: unknown;
  query?: Record<string, string | number | boolean | undefined>;
  signal?: AbortSignal;
}

function buildUrl(path: string, query?: RequestOptions["query"]): string {
  const url = `${PROXY_BASE}${path}`;
  if (!query) return url;
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined) params.set(key, String(value));
  }
  const qs = params.toString();
  return qs ? `${url}?${qs}` : url;
}

async function parseJson(response: Response): Promise<unknown> {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text) as unknown;
  } catch {
    return null;
  }
}

function messageFrom(body: unknown, fallback: string): string {
  if (isRecord(body) && typeof body.message === "string" && body.message)
    return body.message;
  return fallback;
}

/**
 * Calls the backend through the BFF proxy and returns the unwrapped `data`.
 * Throws ApiError for non-2xx responses (envelope or security-handler shape).
 */
export async function request<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const { method = "GET", body, query, signal } = options;
  const response = await fetch(buildUrl(path, query), {
    method,
    signal,
    headers:
      body === undefined ? undefined : { "Content-Type": "application/json" },
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  const json = await parseJson(response);
  if (!response.ok) {
    throw new ApiError(
      response.status,
      messageFrom(json, response.statusText || "Request failed"),
    );
  }
  if (isRecord(json) && "data" in json) return json.data as T;
  return null as T;
}
