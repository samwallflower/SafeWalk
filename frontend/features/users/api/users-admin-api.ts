import type { UserDto } from "@/features/auth/types";
import { request } from "@/lib/http/request";

import type { CreateUserValues, EditUserValues } from "../schemas/user-schemas";

export const usersAdminApi = {
  /** Admin only. Small today (accounts), unlike incident reports. */
  listAll: (signal?: AbortSignal) => request<UserDto[]>("/users/all", { signal }),
  /** Same endpoint people use to sign up. The new person gets a verification code by email. */
  create: (values: CreateUserValues) => request<UserDto>("/users/register", { method: "POST", body: values }),
  update: (id: number, values: EditUserValues) =>
    request<UserDto>(`/users/${id}/update`, {
      method: "PUT",
      body: { firstName: values.firstName, lastName: values.lastName, phoneNumber: values.phoneNumber || undefined },
    }),
  remove: (id: number) => request<null>(`/users/${id}/delete`, { method: "DELETE" }),
};
