import { ApiError } from "@/lib/http/api-error";
import { request } from "@/lib/http/request";

import type { IncidentVoteDto, VoteType } from "../types";

const base = (userId: number, reportId: number) =>
  `/incident-votes/user/${userId}/report/${reportId}`;

export const votesApi = {
  /** How many votes the user has cast (dashboard stat). */
  countMine: (userId: number, signal?: AbortSignal) =>
    request<number>(`/incident-votes/count/all-by-user/user/${userId}/vote`, {
      signal,
    }),
  /** The signed-in user's vote on a report, or null when they have not voted (backend answers 404). */
  mine: async (
    userId: number,
    reportId: number,
    signal?: AbortSignal,
  ): Promise<VoteType | null> => {
    try {
      const vote = await request<IncidentVoteDto>(
        `${base(userId, reportId)}/vote`,
        { signal },
      );
      return vote.voteType;
    } catch (error) {
      if (error instanceof ApiError && error.status === 404) return null;
      throw error;
    }
  },
  cast: (userId: number, reportId: number, voteType: VoteType) =>
    request<IncidentVoteDto>(`${base(userId, reportId)}/cast`, {
      method: "POST",
      query: { voteType },
    }),
  update: (userId: number, reportId: number, voteType: VoteType) =>
    request<IncidentVoteDto>(`${base(userId, reportId)}/update`, {
      method: "PUT",
      query: { voteType },
    }),
  remove: (userId: number, reportId: number) =>
    request<null>(`${base(userId, reportId)}/remove`, { method: "DELETE" }),
};
