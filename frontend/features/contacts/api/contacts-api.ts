import { request } from "@/lib/http/request";

import type { EmergencyContact } from "../types";

interface ContactBody {
  contactName: string;
  contactEmail: string;
  contactPhone?: string;
}

const base = (userId: number) => `/emergency-contacts/${userId}`;

export const contactsApi = {
  list: (userId: number, signal?: AbortSignal) => request<EmergencyContact[]>(`${base(userId)}/contacts`, { signal }),
  add: (userId: number, body: ContactBody) => request<EmergencyContact>(`${base(userId)}/add`, { method: "POST", body }),
  update: (userId: number, contactId: number, body: ContactBody) =>
    request<EmergencyContact>(`${base(userId)}/contacts/${contactId}/update`, { method: "PUT", body }),
  remove: (userId: number, contactId: number) => request<null>(`${base(userId)}/contacts/${contactId}/delete`, { method: "DELETE" }),
};
