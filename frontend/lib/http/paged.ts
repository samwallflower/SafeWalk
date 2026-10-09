import type { PageResponse } from "./page";
import { request } from "./request";

/** The backend caps a page at 50 rows (PageResponse.MAX_PAGE_SIZE). */
export const MAX_PAGE_SIZE = 50;

export interface AllPages<T> {
  items: T[];
  /** Total rows on the server, which can exceed `items.length` when `truncated`. */
  total: number;
  truncated: boolean;
}

interface AllPagesOptions {
  query?: Record<string, string | number | boolean | undefined>;
  signal?: AbortSignal;
  /** Safety cap so one call can never pull an unbounded table. */
  maxItems?: number;
}

/**
 * Reads a paged list endpoint into one array. Page 0 tells us how many pages exist,
 * the rest are fetched together. Only for lists that are small by nature (a user's own
 * reports, a moderation queue, a bounded time window); use real paging for anything big.
 */
export async function requestAllPages<T>(
  path: string,
  { query, signal, maxItems = 1000 }: AllPagesOptions = {},
): Promise<AllPages<T>> {
  const fetchPage = (page: number) =>
    request<PageResponse<T>>(path, {
      query: { ...query, page, size: MAX_PAGE_SIZE },
      signal,
    });

  const first = await fetchPage(0);
  const pageLimit = Math.max(1, Math.ceil(maxItems / MAX_PAGE_SIZE));
  const lastPage = Math.min(first.totalPages, pageLimit) - 1;
  const rest = await Promise.all(
    Array.from({ length: Math.max(0, lastPage) }, (_, i) => fetchPage(i + 1)),
  );

  return {
    items: [first, ...rest].flatMap((p) => p.content),
    total: first.totalElements,
    truncated: first.totalPages > pageLimit,
  };
}
