import { afterEach, describe, expect, it, vi } from "vitest";

import { requestAllPages } from "./paged";

function page(
  content: number[],
  pageNumber: number,
  totalPages: number,
  totalElements: number,
) {
  return new Response(
    JSON.stringify({
      message: "ok",
      data: {
        content,
        page: pageNumber,
        size: 50,
        totalElements,
        totalPages,
        last: pageNumber === totalPages - 1,
      },
    }),
    {
      status: 200,
      headers: { "content-type": "application/json" },
    },
  );
}

afterEach(() => vi.unstubAllGlobals());

describe("requestAllPages", () => {
  it("joins every page in order and reports the total", async () => {
    const fetchMock = vi.fn(async (url: string) => {
      const n = Number(new URL(url, "http://x").searchParams.get("page"));
      return page([n * 2, n * 2 + 1], n, 3, 6);
    });
    vi.stubGlobal("fetch", fetchMock);
    const result = await requestAllPages<number>("/things");
    expect(result).toEqual({
      items: [0, 1, 2, 3, 4, 5],
      total: 6,
      truncated: false,
    });
    expect(fetchMock).toHaveBeenCalledTimes(3);
  });

  it("stops at maxItems and says it truncated", async () => {
    const fetchMock = vi.fn(async (url: string) =>
      page(
        [Number(new URL(url, "http://x").searchParams.get("page"))],
        0,
        100,
        5000,
      ),
    );
    vi.stubGlobal("fetch", fetchMock);
    const result = await requestAllPages<number>("/things", { maxItems: 100 });
    expect(fetchMock).toHaveBeenCalledTimes(2);
    expect(result.truncated).toBe(true);
    expect(result.total).toBe(5000);
  });
});
