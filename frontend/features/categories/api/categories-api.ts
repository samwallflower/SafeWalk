import { request } from "@/lib/http/request";

import type { IncidentCategory } from "../types";

export const categoriesApi = {
  list: (signal?: AbortSignal) => request<IncidentCategory[]>("/incident-categories/all", { signal }),
};
