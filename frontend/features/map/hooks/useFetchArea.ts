"use client";

import { useState } from "react";

import { nextFetchArea } from "../lib/area";
import type { Circle } from "../types";

/**
 * Stable area to fetch for the current viewport circle. Recomputed during render
 * (no effect) and only changes when the view leaves it or it is far too large.
 */
export function useFetchArea(view: Circle | null, maxRadiusM: number): Circle | null {
  const [area, setArea] = useState<Circle | null>(null);
  const next = nextFetchArea(area, view, maxRadiusM);
  if (next !== area) setArea(next);
  return next;
}
