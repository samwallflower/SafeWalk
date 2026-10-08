"use client";

import { SearchIcon } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { IncidentCategory } from "@/features/categories/types";
import type { ReportStatus } from "@/features/incidents/types";
import type { TimeRange } from "@/features/map/types";

import {
  DEFAULT_FILTERS,
  filtersAreDefault,
  type ExplorerFilterValues,
  type SortKey,
} from "../lib/explorer-filters";

const RANGES: { value: TimeRange; label: string }[] = [
  { value: "all", label: "All time" },
  { value: "24h", label: "Last 24 hours" },
  { value: "7d", label: "Last 7 days" },
  { value: "30d", label: "Last 30 days" },
];
const SORTS: { value: SortKey; label: string }[] = [
  { value: "newest", label: "Newest first" },
  { value: "oldest", label: "Oldest first" },
  { value: "most-upvoted", label: "Most upvoted" },
  { value: "most-downvoted", label: "Most downvoted" },
];
const STATUSES: { value: ReportStatus; label: string }[] = [
  { value: "ACTIVE", label: "Active" },
  { value: "UNDER_REVIEW", label: "Under review" },
  { value: "HIDDEN", label: "Hidden" },
];

const trigger = "h-9 w-full border-transparent bg-muted";

const isRange = (v: string): v is TimeRange =>
  RANGES.some((r) => r.value === v);
const isSort = (v: string): v is SortKey => SORTS.some((s) => s.value === v);
const isStatus = (v: string): v is ReportStatus =>
  STATUSES.some((s) => s.value === v);

interface ExplorerFiltersProps {
  categories: readonly IncidentCategory[];
  filters: ExplorerFilterValues;
  onChange: (filters: ExplorerFilterValues) => void;
  /** Only admins can look at non-active reports. */
  canChooseStatus: boolean;
  status: ReportStatus;
  onStatus: (status: ReportStatus) => void;
}

export function ExplorerFilters({
  categories,
  filters,
  onChange,
  canChooseStatus,
  status,
  onStatus,
}: ExplorerFiltersProps) {
  const categoryItems = [
    { value: "all", label: "All categories" },
    ...categories.map((c) => ({ value: String(c.id), label: c.name })),
  ];

  return (
    <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-[minmax(0,1.4fr)_repeat(3,minmax(0,1fr))_auto]">
      <div className="relative">
        <SearchIcon
          className="pointer-events-none absolute top-1/2 left-2.5 size-4 -translate-y-1/2 text-muted-foreground"
          aria-hidden="true"
        />
        <Input
          aria-label="Search reports"
          placeholder="Search description, category or #id"
          className="h-9 border-transparent bg-muted pl-8"
          value={filters.query}
          onChange={(e) => onChange({ ...filters, query: e.target.value })}
        />
      </div>
      <Select
        items={categoryItems}
        value={filters.categoryId === null ? "all" : String(filters.categoryId)}
        onValueChange={(v) =>
          onChange({
            ...filters,
            categoryId: v === null || v === "all" ? null : Number(v),
          })
        }
      >
        <SelectTrigger className={trigger} aria-label="Filter by category">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {categoryItems.map((i) => (
            <SelectItem key={i.value} value={i.value}>
              {i.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <Select
        items={RANGES}
        value={filters.range}
        onValueChange={(v) =>
          v !== null && isRange(v) && onChange({ ...filters, range: v })
        }
      >
        <SelectTrigger className={trigger} aria-label="Filter by time">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {RANGES.map((i) => (
            <SelectItem key={i.value} value={i.value}>
              {i.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <Select
        items={SORTS}
        value={filters.sort}
        onValueChange={(v) =>
          v !== null && isSort(v) && onChange({ ...filters, sort: v })
        }
      >
        <SelectTrigger className={trigger} aria-label="Sort reports">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {SORTS.map((i) => (
            <SelectItem key={i.value} value={i.value}>
              {i.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      {filtersAreDefault(filters) &&
      filters.sort === DEFAULT_FILTERS.sort ? null : (
        <Button
          type="button"
          variant="ghost"
          size="sm"
          className="h-9"
          onClick={() => onChange(DEFAULT_FILTERS)}
        >
          Clear
        </Button>
      )}
      {canChooseStatus ? (
        <Select
          items={STATUSES}
          value={status}
          onValueChange={(v) => v !== null && isStatus(v) && onStatus(v)}
        >
          <SelectTrigger className={trigger} aria-label="Report status (admin)">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {STATUSES.map((i) => (
              <SelectItem key={i.value} value={i.value}>
                {i.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      ) : null}
    </div>
  );
}
