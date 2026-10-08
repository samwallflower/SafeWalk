"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { useSession } from "@/features/auth/hooks/useSession";
import type { Incident } from "@/features/incidents/types";

import { votesApi } from "../api/votes-api";
import { applyVoteChange, nextVote } from "../lib/vote-counts";
import type { VoteType } from "../types";
import { myVoteKey, useMyVote } from "./useMyVote";

interface VoteChange {
  from: VoteType | null;
  to: VoteType | null;
}

const detailKey = (reportId: number) => ["incidents", "detail", reportId] as const;
const NEARBY_KEY = ["incidents", "nearby"] as const;

function patchCounts(incident: Incident, reportId: number, change: VoteChange): Incident {
  return incident.id === reportId ? applyVoteChange(incident, change.from, change.to) : incident;
}

/** Cast / switch / remove a vote with optimistic counts and rollback. */
export function useVote(reportId: number) {
  const queryClient = useQueryClient();
  const { user } = useSession();
  const myVote = useMyVote(reportId);
  const current = myVote.data ?? null;

  const mutation = useMutation({
    mutationKey: ["votes", "change", reportId],
    mutationFn: ({ from, to }: VoteChange) => {
      const userId = user!.id;
      if (to === null) return votesApi.remove(userId, reportId);
      return from === null ? votesApi.cast(userId, reportId, to) : votesApi.update(userId, reportId, to);
    },
    onMutate: async (change) => {
      const mineKey = myVoteKey(user!.id, reportId);
      await queryClient.cancelQueries({ queryKey: mineKey });
      await queryClient.cancelQueries({ queryKey: detailKey(reportId) });

      const previousMine = queryClient.getQueryData<VoteType | null>(mineKey);
      const previousDetail = queryClient.getQueryData<Incident>(detailKey(reportId));
      const previousNearby = queryClient.getQueriesData<Incident[]>({ queryKey: NEARBY_KEY });

      queryClient.setQueryData(mineKey, change.to);
      queryClient.setQueryData<Incident>(detailKey(reportId), (old) => old && patchCounts(old, reportId, change));
      queryClient.setQueriesData<Incident[]>({ queryKey: NEARBY_KEY }, (old) =>
        old?.map((i) => patchCounts(i, reportId, change)),
      );
      return { mineKey, previousMine, previousDetail, previousNearby };
    },
    onError: (error, _change, context) => {
      if (context) {
        queryClient.setQueryData(context.mineKey, context.previousMine);
        queryClient.setQueryData(detailKey(reportId), context.previousDetail);
        for (const [key, data] of context.previousNearby) queryClient.setQueryData(key, data);
      }
      toast.error(error.message.replace(/^Error:\s*/, ""));
    },
    onSettled: () => {
      if (user) void queryClient.invalidateQueries({ queryKey: myVoteKey(user.id, reportId) });
      void queryClient.invalidateQueries({ queryKey: detailKey(reportId) });
    },
  });

  return {
    canVote: user !== null,
    myVote: current,
    isPending: mutation.isPending || myVote.isPending,
    vote: (clicked: VoteType) => mutation.mutate({ from: current, to: nextVote(current, clicked) }),
  };
}
