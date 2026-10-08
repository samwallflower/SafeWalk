"use client";

import { useCallback, useState } from "react";

import type { LatLng } from "@/lib/geo/haversine";

export type GeolocationStatus = "idle" | "locating" | "denied" | "unavailable" | "error";

/** Location is only requested when `locate()` is called from a user action. */
export function useGeolocation() {
  const [status, setStatus] = useState<GeolocationStatus>("idle");
  const [position, setPosition] = useState<LatLng | null>(null);

  const locate = useCallback((onFound?: (coords: LatLng) => void) => {
    if (!("geolocation" in navigator)) {
      setStatus("unavailable");
      return;
    }
    setStatus("locating");
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        const found = { latitude: coords.latitude, longitude: coords.longitude };
        setPosition(found);
        setStatus("idle");
        onFound?.(found);
      },
      (error) => setStatus(error.code === error.PERMISSION_DENIED ? "denied" : "error"),
      { enableHighAccuracy: true, timeout: 10_000 },
    );
  }, []);

  return { status, position, locate };
}
