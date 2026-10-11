import { toIncident } from "@/features/incidents/api/incident-mappers";
import type { Incident, IncidentReportDto, ReportStatus } from "@/features/incidents/types";
import { isRecord } from "@/lib/http/envelope";
import type { PageResponse } from "@/lib/http/page";
import { request } from "@/lib/http/request";

export type StatusFilter = ReportStatus | "ALL";

/** A report as an admin sees it: the usual model plus who filed it, which the server needs to accept an edit. */
export interface AdminReport extends Incident {
  reporterId: number | null;
}

export interface AdminReportsPage {
  items: AdminReport[];
  total: number;
  totalPages: number;
}

export interface AdminReportEdit {
  description: string;
  isAnonymous: boolean;
  category: { id: number; name: string };
}

function toAdminReport(dto: IncidentReportDto): AdminReport {
  const id = isRecord(dto.user) && typeof dto.user.id === "number" ? dto.user.id : null;
  return { ...toIncident(dto), reporterId: id };
}

export const reportsAdminApi = {
  /** One server page. `page` is zero-based. Safe on the whole table (about 67k reports): never more than one page is read. */
  page: async (status: StatusFilter, page: number, size: number, signal?: AbortSignal): Promise<AdminReportsPage> => {
    const result =
      status === "ALL"
        ? await request<PageResponse<IncidentReportDto>>("/incident-reports/all", { query: { page, size }, signal })
        : await request<PageResponse<IncidentReportDto>>("/incident-reports/by-status/report", { query: { status, page, size }, signal });
    return { items: result.content.map(toAdminReport), total: result.totalElements, totalPages: result.totalPages };
  },
  /** Admins may edit anyone's report, but the endpoint is addressed by the reporter's id. */
  update: async (reporterId: number, reportId: number, body: AdminReportEdit): Promise<AdminReport> =>
    toAdminReport(await request<IncidentReportDto>(`/incident-reports/${reporterId}/report/${reportId}/update`, { method: "PUT", body })),
};
