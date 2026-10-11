import type { SessionStatus, WalkSession } from "@/features/sessions/types";
import { request } from "@/lib/http/request";

export const sessionsAdminApi = {
  list: (signal?: AbortSignal) => request<WalkSession[]>("/walk-sessions/all", { signal }),
  end: (id: number) => request<WalkSession>(`/walk-sessions/${id}/session/end`, { method: "PUT" }),
  /** The server refuses COMPLETED here (use `end`) and sessions that already ended. */
  setStatus: (id: number, status: Exclude<SessionStatus, "COMPLETED">) =>
    request<WalkSession>(`/walk-sessions/${id}/session/update/by-status`, { method: "PUT", query: { status } }),
  remove: (id: number, userId: number) => request<null>(`/walk-sessions/${id}/session/user/${userId}/delete`, { method: "DELETE" }),
};
