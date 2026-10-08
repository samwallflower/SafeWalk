"use client";

import { preconnect } from "react-dom";

import { useCallback, useRef, useState } from "react";
import type { MapRef } from "react-map-gl/mapbox";

import { useCategories } from "@/features/categories/hooks/useCategories";
import { PlaceSearch } from "@/features/places/components/PlaceSearch";

import { MAX_POINTS_RADIUS_M } from "../config";
import { useGeolocation } from "@/hooks/useGeolocation";
import { useReverseGeocode } from "@/hooks/useReverseGeocode";
import { useMapData } from "../hooks/useMapData";
import { viewportCircle } from "../lib/area";
import { useMapUi } from "../store/mapFilters";
import type { Coordinates, Viewport } from "../types";
import { LocationCard } from "./LocationCard";
import { MapFilters } from "./MapFilters";
import { MapStatus } from "./MapStatus";
import { MapSummary } from "./MapSummary";
import { MapTopBar } from "./MapTopBar";
import { MapViewLazy } from "./MapViewLazy";
import { NearbyIncidentsList } from "./NearbyIncidentsList";
import { ReportCta } from "./ReportCta";

const FLY_ZOOM = 14;

export function MapScreen() {
  // Start the Mapbox connection while the map code is still loading.
  preconnect("https://api.mapbox.com");
  const mapRef = useRef<MapRef>(null);
  const [viewport, setViewport] = useState<Viewport | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const { categoryId, range, selectedIncidentId, hoveredIncidentId, select, hover, setLastCenter, lastCenter } = useMapUi();
  const categories = useCategories();
  const geo = useGeolocation();
  const address = useReverseGeocode(geo.position);

  const data = useMapData(viewport, { categoryId, range });
  const zoomedIn = viewport !== null && viewportCircle(viewport.bounds).radiusMeters <= MAX_POINTS_RADIUS_M;

  const flyTo = useCallback((target: Coordinates) => {
    mapRef.current?.flyTo({ center: [target.longitude, target.latitude], zoom: FLY_ZOOM, duration: 1200 });
  }, []);

  const handleViewport = useCallback(
    (next: Viewport) => {
      setViewport(next);
      setLastCenter({
        latitude: (next.bounds.north + next.bounds.south) / 2,
        longitude: (next.bounds.east + next.bounds.west) / 2,
      });
    },
    [setLastCenter],
  );

  /** Pan so the popup card (which opens above the dot) clears the top control bar. */
  const handleSelect = useCallback(
    (id: number | null) => {
      select(id);
      const incident = id === null ? null : data.incidents.find((i) => i.id === id);
      if (incident) {
        mapRef.current?.easeTo({ center: [incident.longitude, incident.latitude], offset: [0, 150], duration: 400 });
      }
    },
    [select, data.incidents],
  );

  return (
    <div className="relative h-[calc(100dvh-4rem)] w-full">
      <MapViewLazy
        mapRef={mapRef}
        heatPoints={data.heatPoints}
        incidents={data.incidents}
        userLocation={geo.position}
        selectedId={selectedIncidentId}
        hoveredId={hoveredIncidentId}
        onViewportChange={handleViewport}
        onSelect={handleSelect}
        onHover={hover}
      />

      <div className="pointer-events-none absolute inset-x-0 top-0 p-3 md:p-4">
        <div className="pointer-events-auto mx-auto max-w-7xl">
          <MapTopBar>
            <LocationCard
              status={geo.status}
              hasPosition={geo.position !== null}
              address={address.data}
              onLocate={() => geo.locate(flyTo)}
              onRecenter={() => geo.position && flyTo(geo.position)}
            />
            <PlaceSearch
              query={searchQuery}
              onQueryChange={setSearchQuery}
              onSelect={flyTo}
              proximity={lastCenter ?? undefined}
              ariaLabel="Search for a place"
              placeholder="Search city, address, or station to inspect…"
            />
            {categories.isError ? (
              <p className="px-2 text-xs text-destructive">Categories failed to load; filters unavailable.</p>
            ) : (
              <MapFilters categories={categories.data ?? []} />
            )}
          </MapTopBar>
        </div>
      </div>

      <div className="pointer-events-none absolute inset-x-0 top-1/2 flex justify-center px-3">
        <div className="pointer-events-auto">
          <MapStatus
            zoomedOut={data.zoomedOut}
            filtersNeedZoom={data.filtersNeedZoom}
            isLoading={data.isLoading}
            isFetching={data.isFetching}
            isEmpty={viewport !== null && data.heatPoints.length === 0}
            error={data.error}
            onRetry={data.retry}
          />
        </div>
      </div>

      <div className="pointer-events-none absolute bottom-4 left-3 md:left-4">
        <div className="pointer-events-auto">
          <MapSummary
            count={data.heatPoints.length}
            zoomedOut={data.zoomedOut}
            action={<NearbyIncidentsList incidents={data.incidents} zoomedIn={zoomedIn} />}
          />
        </div>
      </div>

      <div className="pointer-events-none absolute right-3 bottom-4 md:right-4">
        <div className="pointer-events-auto">
          <ReportCta />
        </div>
      </div>
    </div>
  );
}
