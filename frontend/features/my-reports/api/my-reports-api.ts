import { toIncident } from "@/features/incidents/api/incident-mappers";
import type { Incident, IncidentReportDto } from "@/features/incidents/types";
import { requestAllPages } from "@/lib/http/paged";
import { request } from "@/lib/http/request";

interface UpdateReportBody {
  description: string;
  isAnonymous: boolean;
  category: { id: number; name: string };
}

const base = (userId: number) => `/incident-reports/${userId}/report`;

export const myReportsApi = {
  list: async (userId: number, signal?: AbortSignal): Promise<Incident[]> => {
    const { items } = await requestAllPages<IncidentReportDto>(
      `/incident-reports/user/${userId}/report`,
      { signal, maxItems: 500 },
    );
    return items.map(toIncident);
  },
  update: async (
    userId: number,
    reportId: number,
    body: UpdateReportBody,
  ): Promise<Incident> =>
    toIncident(
      await request<IncidentReportDto>(`${base(userId)}/${reportId}/update`, {
        method: "PUT",
        body,
      }),
    ),
  remove: (userId: number, reportId: number) =>
    request<null>(`${base(userId)}/${reportId}/delete`, { method: "DELETE" }),
};
