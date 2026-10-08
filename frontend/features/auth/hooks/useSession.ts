"use client";

import { useQuery } from "@tanstack/react-query";

import { isAdmin, type SessionUser } from "@/lib/auth/session-types";

import { authApi } from "../api/auth-api";

export const SESSION_QUERY_KEY = ["auth", "session"] as const;

export interface UseSessionResult {
  user: SessionUser | null;
  isAdmin: boolean;
  isLoading: boolean;
  isError: boolean;
  refetch: () => void;
}

/** The only source of `userId` for any `/user/{userId}` call. */
export function useSession(): UseSessionResult {
  const query = useQuery({ queryKey: SESSION_QUERY_KEY, queryFn: authApi.session, staleTime: 60_000 });
  const user = query.data ?? null;
  return {
    user,
    isAdmin: user ? isAdmin(user) : false,
    isLoading: query.isPending,
    isError: query.isError,
    refetch: () => void query.refetch(),
  };
}
