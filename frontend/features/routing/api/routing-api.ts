import { request } from "@/lib/http/request";

import type { RouteDto, RouteRecommendationBody } from "../types";

export const routingApi = {
  /** Ranked alternatives; rank 1 has the lowest virtual distance (distance + safety penalty). */
  recommend: (body: RouteRecommendationBody, signal?: AbortSignal) =>
    request<RouteDto[]>("/routing/recommend", { method: "POST", body, signal }),
  getById: (routeId: number, signal?: AbortSignal) =>
    request<RouteDto>(`/routing/${routeId}/route`, { signal }),
};
