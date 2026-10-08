"use client";

import { SearchIcon } from "lucide-react";
import { useId, useState, type KeyboardEvent } from "react";

import { Input } from "@/components/ui/input";
import type { LatLng } from "@/lib/geo/haversine";
import type { GeocodeResult } from "@/lib/mapbox/geocode";
import { cn } from "@/lib/utils";

import { useGeocodeSearch } from "../hooks/useGeocodeSearch";

interface PlaceSearchProps {
  query: string;
  onQueryChange: (query: string) => void;
  onSelect: (result: GeocodeResult) => void;
  ariaLabel: string;
  placeholder: string;
  inputClassName?: string;
  /** Ranks results near this point first. */
  proximity?: LatLng;
}

/** Mapbox place autocomplete with keyboard navigation (combobox pattern). */
export function PlaceSearch({ query, onQueryChange, onSelect, ariaLabel, placeholder, inputClassName, proximity }: PlaceSearchProps) {
  const listId = useId();
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(0);
  const search = useGeocodeSearch(query, proximity);
  const results = search.data ?? [];
  const showList = open && search.enabled;

  const choose = (result: GeocodeResult) => {
    onQueryChange(result.label);
    setOpen(false);
    onSelect(result);
  };

  const onKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "ArrowDown") {
      event.preventDefault();
      setActive((i) => Math.min(i + 1, results.length - 1));
    } else if (event.key === "ArrowUp") {
      event.preventDefault();
      setActive((i) => Math.max(i - 1, 0));
    } else if (event.key === "Enter" && showList && results[active]) {
      event.preventDefault();
      choose(results[active]);
    } else if (event.key === "Escape") {
      setOpen(false);
    }
  };

  return (
    <div className="relative w-full">
      <SearchIcon className="pointer-events-none absolute top-3 left-2.5 size-4 text-muted-foreground" aria-hidden="true" />
      <Input
        role="combobox"
        aria-expanded={showList}
        aria-controls={listId}
        aria-autocomplete="list"
        aria-label={ariaLabel}
        placeholder={placeholder}
        className={cn("h-10 border-transparent bg-muted pl-8", inputClassName)}
        value={query}
        onChange={(e) => {
          onQueryChange(e.target.value);
          setOpen(true);
          setActive(0);
        }}
        onFocus={() => setOpen(true)}
        onBlur={() => setTimeout(() => setOpen(false), 150)}
        onKeyDown={onKeyDown}
      />
      {showList ? (
        <ul id={listId} role="listbox" className="absolute z-20 mt-1 w-full overflow-hidden rounded-xl border bg-card shadow-md">
          {search.isFetching || !search.settled ? (
            <li className="px-3 py-2 text-sm text-muted-foreground">Searching…</li>
          ) : search.isError ? (
            <li className="px-3 py-2 text-sm text-destructive">Search failed. Try again.</li>
          ) : results.length === 0 ? (
            <li className="px-3 py-2 text-sm text-muted-foreground">No places found</li>
          ) : (
            results.map((result, index) => (
              <li
                key={result.id}
                role="option"
                aria-selected={index === active}
                className={cn("cursor-pointer px-3 py-2 text-sm", index === active && "bg-muted")}
                onMouseDown={(e) => {
                  e.preventDefault();
                  choose(result);
                }}
                onMouseEnter={() => setActive(index)}
              >
                {result.label}
              </li>
            ))
          )}
        </ul>
      ) : null}
    </div>
  );
}
