import { afterEach, describe, expect, it, vi } from "vitest";

import { ApiError } from "./api-error";
import { request } from "./request";

function mockFetch(status: number, body: unknown, statusText = "") {
  const response = new Response(body === null ? null : JSON.stringify(body), {
    status,
    statusText,
  });
  const fn = vi.fn().mockResolvedValue(response);
  vi.stubGlobal("fetch", fn);
  return fn;
}

afterEach(() => vi.unstubAllGlobals());

describe("request", () => {
  it("unwraps the envelope data", async () => {
    mockFetch(200, { message: "ok", data: { id: 1 } });
    await expect(request<{ id: number }>("/x")).resolves.toEqual({ id: 1 });
  });

  it("builds the proxy url with query params and skips undefined", async () => {
    const fn = mockFetch(200, { message: "ok", data: [] });
    await request("/x", { query: { a: 1, b: undefined, c: "z" } });
    expect(fn.mock.calls[0][0]).toBe("/api/proxy/x?a=1&c=z");
  });

  it("throws ApiError with the envelope message", async () => {
    mockFetch(409, { message: "Already voted", data: null });
    await expect(request("/x")).rejects.toMatchObject({ status: 409, message: "Already voted" });
  });

  it("handles security-handler error bodies", async () => {
    mockFetch(403, { status: 403, error: "Access Denied", message: "No permission" });
    await expect(request("/x")).rejects.toBeInstanceOf(ApiError);
  });

  it("falls back to status text when the body is empty", async () => {
    mockFetch(401, null, "Unauthorized");
    await expect(request("/x")).rejects.toMatchObject({ status: 401, message: "Unauthorized" });
  });
});
