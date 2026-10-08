"use client";

import { preconnect } from "react-dom";

import { useCallback, useMemo, useRef, useState } from "react";
import type { MapRef } from "react-map-gl/mapbox";

import { DEFAULT_VIEW } from "@/features/map/config";
import { useMapUi } from "@/features/map/store/mapFilters";
import type { Place } from "@/features/places/types";
import { useGeolocation } from "@/hooks/useGeolocation";

import { useRecommendRoutes } from "../hooks/useRecommendRoutes";
import { useRouteIncidents } from "../hooks/useRouteIncidents";
import { boundsOf, decodeRoutes } from "../lib/route-geometry";
import { PlanForm } from "./PlanForm";
import { RouteResults } from "./RouteResults";
import { RoutesMapLazy } from "./RoutesMapLazy";
import { SafetyExplainer } from "./SafetyExplainer";

const EMPTY_POINTS: never[] = [];

export function PlanScreen() {
  // Start the Mapbox connection while the map code is still loading.
  preconnect("https://api.mapbox.com");
  const mapRef = useRef<MapRef>(null);
  const [origin, setOrigin] = useState<Place | null>(null);
  const [destination, setDestination] = useState<Place | null>(null);
  const [originQuery, setOriginQuery] = useState("");
  const [destinationQuery, setDestinationQuery] = useState("");
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [hoveredId, setHoveredId] = useState<number | null>(null);

  const geo = useGeolocation();
  const lastCenter = useMapUi((st) => st.lastCenter);
  const recommend = useRecommendRoutes();
  const routes = useMemo(() => decodeRoutes(recommend.data ?? []), [recommend.data]);
  const incidents = useRouteIncidents(routes);

  const status = recommend.isIdle ? "idle" : recommend.status;

  const submit = useCallback(() => {
    if (!origin || !destination) return;
    recommend.mutate(
      { origin, destination },
      {
        onSuccess: (result) => {
          const best = [...result].sort((a, b) => a.rank - b.rank)[0];
          setSelectedId(best?.id ?? null);
          const bounds = boundsOf(decodeRoutes(result).flatMap((r) => r.points));
          if (bounds) {
            mapRef.current?.fitBounds([bounds.southWest, bounds.northEast], {
              padding: { top: 60, bottom: 60, left: 60, right: 60 },
              duration: 900,
              maxZoom: 16,
            });
          }
        },
      },
    );
  }, [origin, destination, recommend]);

  const useMyLocation = () =>
    geo.locate((found) => {
      setOrigin({ label: "My location", ...found });
      setOriginQuery("My location");
    });

  const swap = () => {
    setOrigin(destination);
    setDestination(origin);
    setOriginQuery(destinationQuery);
    setDestinationQuery(originQuery);
  };

  const sameSpot =
    origin !== null && destination !== null && origin.latitude === destination.latitude && origin.longitude === destination.longitude;

  return (
    <div className="grid h-[calc(100dvh-4rem)] grid-rows-[minmax(16rem,40%)_1fr] lg:grid-cols-[26rem_1fr] lg:grid-rows-1">
      <aside className="order-2 space-y-4 overflow-y-auto bg-background p-4 lg:order-1">
        <div>
          <h1 className="text-xl font-bold tracking-tight">Plan a safer walk</h1>
          <p className="text-sm text-muted-foreground">Compare walking routes ranked by distance and nearby incidents.</p>
        </div>
        <PlanForm
          originQuery={originQuery}
          destinationQuery={destinationQuery}
          onOriginQuery={(q) => {
            setOriginQuery(q);
            setOrigin(null);
          }}
          onDestinationQuery={(q) => {
            setDestinationQuery(q);
            setDestination(null);
          }}
          origin={origin}
          proximity={lastCenter ?? DEFAULT_VIEW}
          onOrigin={setOrigin}
          onDestination={setDestination}
          onUseMyLocation={useMyLocation}
          onSwap={swap}
          geoStatus={geo.status}
          canSubmit={origin !== null && destination !== null && !sameSpot}
          isPending={recommend.isPending}
          onSubmit={submit}
        />
        {sameSpot ? (
          <p role="alert" className="text-sm text-destructive">
            Start and destination are the same place.
          </p>
        ) : null}
        <RouteResults
          status={status}
          error={recommend.error}
          routes={routes}
          selectedId={selectedId}
          onSelect={setSelectedId}
          onHover={setHoveredId}
          onRetry={submit}
        />
        {routes.length > 0 ? <SafetyExplainer /> : null}
      </aside>

      <div className="relative order-1 min-h-0 lg:order-2">
        <RoutesMapLazy
          mapRef={mapRef}
          routes={routes}
          selectedId={selectedId}
          hoveredId={hoveredId}
          origin={origin}
          destination={destination}
          incidents={incidents.data ?? EMPTY_POINTS}
          onSelect={setSelectedId}
        />
      </div>
    </div>
  );
}
