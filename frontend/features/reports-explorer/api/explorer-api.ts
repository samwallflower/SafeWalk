import { toIncident } from "@/features/incidents/api/incident-mappers";
import type {
  Incident,
  IncidentReportDto,
  ReportStatus,
} from "@/features/incidents/types";
import { request } from "@/lib/http/request";

export interface AreaQuery {
  latitude: number;
  longitude: number;
  radiusMeters: number;
}

/**
 * Reports inside a bounded area. Everyone gets ACTIVE reports; admins can also ask for other statuses.
 * The unbounded filter endpoints (by category, by time range, by votes) are deliberately not used:
 * with ~67k active reports they could return thousands of full records.
 */
export const explorerApi = {
  inArea: async (
    area: AreaQuery,
    status: ReportStatus,
    signal?: AbortSignal,
  ): Promise<Incident[]> => {
    const dtos =
      status === "ACTIVE"
        ? await request<IncidentReportDto[]>(
            "/incident-reports/by-location-and-status-active/report",
            { query: { ...area }, signal },
          )
        : await request<IncidentReportDto[]>(
            "/incident-reports/by-location-and-status/report",
            { query: { ...area, status }, signal },
          );
    return dtos.map(toIncident);
  },
};
