import { request } from "@/lib/http/request";

import type { Emergency } from "../types";

export const emergenciesApi = {
  forSession: (userId: number, sessionId: number, signal?: AbortSignal) =>
    request<Emergency[]>(`/emergency/session/${sessionId}/user/${userId}/all`, { signal }),
};
