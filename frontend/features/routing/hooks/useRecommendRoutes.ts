"use client";

import { useMutation } from "@tanstack/react-query";

import type { Place } from "@/features/places/types";

import { routingApi } from "../api/routing-api";

interface RecommendInput {
  origin: Place;
  destination: Place;
}

export function useRecommendRoutes() {
  return useMutation({
    mutationKey: ["routing", "recommend"],
    mutationFn: ({ origin, destination }: RecommendInput) =>
      routingApi.recommend({
        originLatitude: origin.latitude,
        originLongitude: origin.longitude,
        destinationLatitude: destination.latitude,
        destinationLongitude: destination.longitude,
      }),
  });
}
