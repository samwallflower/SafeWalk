import { format, subDays } from "date-fns";

import { toIncident } from "@/features/incidents/api/incident-mappers";
import type { Incident, IncidentReportDto } from "@/features/incidents/types";
import { request } from "@/lib/http/request";

import { WINDOW_DAYS, type StatsWindow } from "../lib/admin-stats";

const STAMP = "yyyy-MM-dd'T'HH:mm:ss";

export const windowReportsApi = {
  /**
   * ACTIVE reports inside a bounded time window. The window is what keeps this safe:
   * the full ACTIVE set (~67k) must never be requested.
   */
  activeIn: async (
    window: StatsWindow,
    signal?: AbortSignal,
  ): Promise<Incident[]> => {
    const end = new Date();
    const start = subDays(end, WINDOW_DAYS[window]);
    const dtos = await request<IncidentReportDto[]>(
      "/incident-reports/by-time-range-and-status/report",
      {
        query: {
          startTime: format(start, STAMP),
          endTime: format(end, STAMP),
          status: "ACTIVE",
        },
        signal,
      },
    );
    return dtos.map(toIncident);
  },
};
