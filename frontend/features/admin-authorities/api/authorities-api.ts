import { request } from "@/lib/http/request";

import type { AuthorityValues } from "../schemas/authority-schema";
import { toAuthorityBody } from "../schemas/authority-schema";
import type { EmergencyAuthority } from "../types";

export const authoritiesApi = {
  list: (signal?: AbortSignal) => request<EmergencyAuthority[]>("/emergency-authority/all", { signal }),
  add: (values: AuthorityValues) => request<EmergencyAuthority>("/emergency-authority/add", { method: "POST", body: toAuthorityBody(values) }),
  update: (id: number, values: AuthorityValues) =>
    request<EmergencyAuthority>(`/emergency-authority/${id}/update`, { method: "PUT", body: toAuthorityBody(values) }),
  remove: (id: number) => request<null>(`/emergency-authority/${id}/delete`, { method: "DELETE" }),
};
