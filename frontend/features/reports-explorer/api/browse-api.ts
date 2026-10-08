import { toIncident } from "@/features/incidents/api/incident-mappers";
import type { Incident, IncidentReportDto } from "@/features/incidents/types";
import { request } from "@/lib/http/request";
import type { PageResponse } from "@/lib/http/page";

export interface IncidentPage {
  items: Incident[];
  /** One-based, to match the table pagination. */
  page: number;
  pageCount: number;
  total: number;
}

/** Server-side paging over ALL active reports, plus the report total. Safe at ~67k rows. */
export const browseApi = {
  activePage: async (
    page: number,
    pageSize: number,
    signal?: AbortSignal,
  ): Promise<IncidentPage> => {
    const result = await request<PageResponse<IncidentReportDto>>(
      "/incident-reports/page/active/report",
      {
        query: { page: page - 1, pageSize },
        signal,
      },
    );
    return {
      items: result.content.map(toIncident),
      page: result.page + 1,
      pageCount: Math.max(1, result.totalPages),
      total: result.totalElements,
    };
  },
  /** Every report regardless of status. */
  countAll: (signal?: AbortSignal) =>
    request<number>("/incident-reports/count/report", { signal }),
};
