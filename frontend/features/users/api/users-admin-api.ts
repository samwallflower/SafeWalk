import type { UserDto } from "@/features/auth/types";
import { request } from "@/lib/http/request";

export const usersAdminApi = {
  /** Admin only. Small today (accounts), unlike incident reports. */
  listAll: (signal?: AbortSignal) => request<UserDto[]>("/users/all", { signal }),
};
