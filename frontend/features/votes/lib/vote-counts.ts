import type { VoteType } from "../types";

export interface VoteCountsValue {
  upvotes: number;
  downvotes: number;
}

/** Next vote state when `clicked` is pressed: same type removes, otherwise cast/switch. */
export function nextVote(
  current: VoteType | null,
  clicked: VoteType,
): VoteType | null {
  return current === clicked ? null : clicked;
}

/** Counts after moving a user's vote from `from` to `to` (either may be null = no vote). */
export function applyVoteChange<T extends VoteCountsValue>(
  counts: T,
  from: VoteType | null,
  to: VoteType | null,
): T {
  let { upvotes, downvotes } = counts;
  if (from === "UPVOTE") upvotes -= 1;
  if (from === "DOWNVOTE") downvotes -= 1;
  if (to === "UPVOTE") upvotes += 1;
  if (to === "DOWNVOTE") downvotes += 1;
  return {
    ...counts,
    upvotes: Math.max(0, upvotes),
    downvotes: Math.max(0, downvotes),
  };
}
