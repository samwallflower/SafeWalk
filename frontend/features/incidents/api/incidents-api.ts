import { request } from "@/lib/http/request";

import type { CreateIncidentBody, HeatPoint, Incident, IncidentReportDto } from "../types";
import { toIncident } from "./incident-mappers";

export interface AreaQuery {
  latitude: number;
  longitude: number;
  radiusMeters: number;
}

/**
 * Only radius-bounded endpoints are used here. `/incident-reports/all` and the
 * other unbounded list endpoints must never be called from the UI (~67k rows).
 */
export const incidentsApi = {
  heatmapPoints: (area: AreaQuery, signal?: AbortSignal) =>
    request<HeatPoint[]>("/incident-reports/heatmap-points/report", { query: { ...area }, signal }),

  nearby: async (area: AreaQuery, signal?: AbortSignal): Promise<Incident[]> => {
    const dtos = await request<IncidentReportDto[]>("/incident-reports/nearby/report", {
      query: { ...area },
      signal,
    });
    return dtos.map(toIncident);
  },

  byId: async (id: number, signal?: AbortSignal): Promise<Incident> =>
    toIncident(await request<IncidentReportDto>(`/incident-reports/${id}/report`, { signal })),

  /** userId must come from the session. Backend resolves the category by name and rate-limits (429). */
  create: async (userId: number, body: CreateIncidentBody): Promise<Incident> =>
    toIncident(await request<IncidentReportDto>(`/incident-reports/${userId}/report/add`, { method: "POST", body })),
};
