"use client";

import { useQuery } from "@tanstack/react-query";

import { incidentsApi } from "../api/incidents-api";

export function useIncident(id: number) {
  return useQuery({
    queryKey: ["incidents", "detail", id],
    queryFn: ({ signal }) => incidentsApi.byId(id, signal),
    enabled: Number.isInteger(id) && id > 0,
  });
}
