"use client";

import { useQuery } from "@tanstack/react-query";

import { useSession } from "@/features/auth/hooks/useSession";

import { votesApi } from "../api/votes-api";

export const myVoteKey = (userId: number, reportId: number) =>
  ["votes", "mine", userId, reportId] as const;

export function useMyVote(reportId: number) {
  const { user } = useSession();
  return useQuery({
    queryKey: myVoteKey(user?.id ?? 0, reportId),
    queryFn: ({ signal }) => votesApi.mine(user!.id, reportId, signal),
    enabled: user !== null,
    staleTime: 60_000,
  });
}
