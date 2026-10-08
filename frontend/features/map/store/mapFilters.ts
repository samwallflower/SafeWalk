import { create } from "zustand";

import type { LatLng } from "@/lib/geo/haversine";

import type { MapFilterValues, TimeRange } from "../types";

interface MapUiState extends MapFilterValues {
  selectedIncidentId: number | null;
  hoveredIncidentId: number | null;
  /** Last map center, used to start the report pin where the user was looking. */
  lastCenter: LatLng | null;
  setCategoryId: (id: number | null) => void;
  setRange: (range: TimeRange) => void;
  resetFilters: () => void;
  select: (id: number | null) => void;
  hover: (id: number | null) => void;
  setLastCenter: (center: LatLng) => void;
}

/** Small UI state only; server data lives in TanStack Query. */
export const useMapUi = create<MapUiState>((set) => ({
  categoryId: null,
  range: "all",
  selectedIncidentId: null,
  hoveredIncidentId: null,
  lastCenter: null,
  setCategoryId: (categoryId) => set({ categoryId, selectedIncidentId: null }),
  setRange: (range) => set({ range, selectedIncidentId: null }),
  resetFilters: () => set({ categoryId: null, range: "all", selectedIncidentId: null }),
  select: (selectedIncidentId) => set({ selectedIncidentId }),
  hover: (hoveredIncidentId) => set({ hoveredIncidentId }),
  setLastCenter: (lastCenter) => set({ lastCenter }),
}));
