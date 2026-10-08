import type { WalkSession } from "@/features/sessions/types";
import { request } from "@/lib/http/request";

export const sessionsAdminApi = {
  list: (signal?: AbortSignal) => request<WalkSession[]>("/walk-sessions/all", { signal }),
  end: (id: number) => request<WalkSession>(`/walk-sessions/${id}/session/end`, { method: "PUT" }),
};
