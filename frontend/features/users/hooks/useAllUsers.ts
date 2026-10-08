"use client";

import { useQuery } from "@tanstack/react-query";

import { usersAdminApi } from "../api/users-admin-api";

export function useAllUsers() {
  return useQuery({
    queryKey: ["admin", "users"],
    queryFn: ({ signal }) => usersAdminApi.listAll(signal),
    staleTime: 60_000,
  });
}
