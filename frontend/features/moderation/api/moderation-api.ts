import { toIncident } from "@/features/incidents/api/incident-mappers";
import type {
  Incident,
  IncidentReportDto,
  ReportStatus,
} from "@/features/incidents/types";
import { requestAllPages } from "@/lib/http/paged";
import { request } from "@/lib/http/request";

/**
 * Admin moderation. Lists are only requested for UNDER_REVIEW and HIDDEN (small);
 * ACTIVE (~67k) is never listed. Any single report is reachable by id.
 */
export const moderationApi = {
  byStatus: async (
    status: Exclude<ReportStatus, "ACTIVE">,
    signal?: AbortSignal,
  ): Promise<Incident[]> => {
    const { items } = await requestAllPages<IncidentReportDto>(
      "/incident-reports/by-status/report",
      { query: { status }, signal, maxItems: 500 },
    );
    return items.map(toIncident);
  },
  byId: async (id: number, signal?: AbortSignal): Promise<Incident> =>
    toIncident(
      await request<IncidentReportDto>(`/incident-reports/${id}/report`, {
        signal,
      }),
    ),
  setStatus: async (id: number, status: ReportStatus): Promise<Incident> =>
    toIncident(
      await request<IncidentReportDto>(
        `/incident-reports/${id}/status/update`,
        { method: "PUT", query: { status } },
      ),
    ),
  remove: (id: number) =>
    request<null>(`/incident-reports/${id}/delete`, { method: "DELETE" }),
};
