import { request } from "@/lib/http/request";

import type { WalkSession } from "../types";

export const sessionsApi = {
  /** Read-only on web: starting and tracking sessions is a mobile feature. */
  listMine: (userId: number, signal?: AbortSignal) =>
    request<WalkSession[]>(`/walk-sessions/user/${userId}/session`, { signal }),
};
