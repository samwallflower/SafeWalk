import type { UserDto } from "@/features/auth/types";
import { request } from "@/lib/http/request";

interface UpdateProfileBody {
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
}

export const usersApi = {
  get: (userId: number, signal?: AbortSignal) => request<UserDto>(`/users/${userId}/user`, { signal }),
  update: (userId: number, body: UpdateProfileBody) => request<UserDto>(`/users/${userId}/update`, { method: "PUT", body }),
};
