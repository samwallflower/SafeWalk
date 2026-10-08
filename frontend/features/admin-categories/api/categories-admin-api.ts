import type { IncidentCategory } from "@/features/categories/types";
import { request } from "@/lib/http/request";

import type { CategoryValues } from "../schemas/category-schema";

export const categoriesAdminApi = {
  add: (body: CategoryValues) => request<IncidentCategory>("/incident-categories/add", { method: "POST", body }),
  update: (id: number, body: CategoryValues) => request<IncidentCategory>(`/incident-categories/${id}/update`, { method: "PUT", body }),
  remove: (id: number) => request<null>(`/incident-categories/${id}/delete`, { method: "DELETE" }),
};
