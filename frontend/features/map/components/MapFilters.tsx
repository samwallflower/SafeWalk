"use client";

import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import type { IncidentCategory } from "@/features/categories/types";

import { filtersActive } from "../lib/filter-incidents";
import { useMapUi } from "../store/mapFilters";
import type { TimeRange } from "../types";

const RANGES: { value: TimeRange; label: string }[] = [
  { value: "all", label: "All time" },
  { value: "24h", label: "Last 24 hours" },
  { value: "7d", label: "Last 7 days" },
  { value: "30d", label: "Last 30 days" },
];

function isTimeRange(value: string): value is TimeRange {
  return RANGES.some((r) => r.value === value);
}

const trigger = "h-9 border-transparent bg-muted";

export function MapFilters({ categories }: { categories: readonly IncidentCategory[] }) {
  const { categoryId, range, setCategoryId, setRange, resetFilters } = useMapUi();
  const categoryItems = [
    { value: "all", label: "All categories" },
    ...categories.map((c) => ({ value: String(c.id), label: c.name })),
  ];

  return (
    <div className="flex flex-wrap items-center gap-2">
      <Select
        items={categoryItems}
        value={categoryId === null ? "all" : String(categoryId)}
        onValueChange={(v) => setCategoryId(v === null || v === "all" ? null : Number(v))}
      >
        <SelectTrigger className={`${trigger} w-44`} aria-label="Filter by category">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {categoryItems.map((item) => (
            <SelectItem key={item.value} value={item.value}>
              {item.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <Select
        items={RANGES}
        value={range}
        onValueChange={(v) => {
          if (v !== null && isTimeRange(v)) setRange(v);
        }}
      >
        <SelectTrigger className={`${trigger} w-36`} aria-label="Filter by time">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {RANGES.map((item) => (
            <SelectItem key={item.value} value={item.value}>
              {item.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      {filtersActive({ categoryId, range }) ? (
        <Button type="button" variant="ghost" size="sm" onClick={resetFilters}>
          Clear
        </Button>
      ) : null}
    </div>
  );
}
