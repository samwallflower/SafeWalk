import type { Emergency, EmergencyTriggerSource } from "@/features/emergencies/types";
import { request } from "@/lib/http/request";

export const emergenciesAdminApi = {
  list: (signal?: AbortSignal) => request<Emergency[]>("/emergency/all", { signal }),
  countBySource: (source: EmergencyTriggerSource, signal?: AbortSignal) =>
    request<number>("/emergency/count-by-trigger-source", { query: { source }, signal }),
  setResolved: (id: number, resolved: boolean) => request<Emergency>(`/emergency/${id}/update`, { method: "PUT", query: { resolved } }),
  remove: (id: number) => request<null>(`/emergency/${id}/delete`, { method: "DELETE" }),
};
