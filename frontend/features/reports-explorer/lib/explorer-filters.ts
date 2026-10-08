import { filterIncidents } from "@/features/map/lib/filter-incidents";
import type { TimeRange } from "@/features/map/types";
import type { Incident } from "@/features/incidents/types";

export type SortKey = "newest" | "oldest" | "most-upvoted" | "most-downvoted";

export interface ExplorerFilterValues {
  categoryId: number | null;
  range: TimeRange;
  /** Free text matched against description, category name and report id. */
  query: string;
  sort: SortKey;
}

export const DEFAULT_FILTERS: ExplorerFilterValues = {
  categoryId: null,
  range: "all",
  query: "",
  sort: "newest",
};

function matchesQuery(report: Incident, query: string): boolean {
  const q = query.trim().toLowerCase();
  if (!q) return true;
  const idQuery = q.replace(/^#?(inc-)?/, "");
  return (
    report.description.toLowerCase().includes(q) ||
    report.category.name.toLowerCase().includes(q) ||
    String(report.id) === idQuery
  );
}

/** Category/time reuse the map's filter; text search is added on top. */
export function filterReports(
  reports: readonly Incident[],
  filters: ExplorerFilterValues,
  nowMs: number = Date.now(),
): Incident[] {
  return filterIncidents(
    reports,
    { categoryId: filters.categoryId, range: filters.range },
    nowMs,
  ).filter((r) => matchesQuery(r, filters.query));
}

export function sortReports(
  reports: readonly Incident[],
  sort: SortKey,
): Incident[] {
  const copy = [...reports];
  switch (sort) {
    case "oldest":
      return copy.sort((a, b) => a.timestamp.localeCompare(b.timestamp));
    case "most-upvoted":
      return copy.sort(
        (a, b) =>
          b.upvotes - a.upvotes || b.timestamp.localeCompare(a.timestamp),
      );
    case "most-downvoted":
      return copy.sort(
        (a, b) =>
          b.downvotes - a.downvotes || b.timestamp.localeCompare(a.timestamp),
      );
    default:
      return copy.sort((a, b) => b.timestamp.localeCompare(a.timestamp));
  }
}

export interface Page<T> {
  items: T[];
  page: number;
  pageCount: number;
  total: number;
}

/** 1-based page, clamped to the valid range. */
export function paginate<T>(
  items: readonly T[],
  page: number,
  size: number,
): Page<T> {
  const pageCount = Math.max(1, Math.ceil(items.length / size));
  const current = Math.min(Math.max(1, page), pageCount);
  return {
    items: items.slice((current - 1) * size, current * size),
    page: current,
    pageCount,
    total: items.length,
  };
}

export function filtersAreDefault(filters: ExplorerFilterValues): boolean {
  return (
    filters.categoryId === null &&
    filters.range === "all" &&
    filters.query.trim() === ""
  );
}
