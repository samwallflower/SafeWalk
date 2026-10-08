"use client";

import { useQuery } from "@tanstack/react-query";

import { useSession } from "@/features/auth/hooks/useSession";

import { votesApi } from "../api/votes-api";

export function useMyVotes() {
  const { user } = useSession();
  return useQuery({
    queryKey: ["me", user?.id, "votes"],
    queryFn: ({ signal }) => votesApi.listMine(user!.id, signal),
    enabled: user !== null,
    staleTime: 60_000,
  });
}
