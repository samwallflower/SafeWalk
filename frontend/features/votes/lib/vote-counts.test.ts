import { describe, expect, it } from "vitest";

import { applyVoteChange, nextVote } from "./vote-counts";

const counts = { upvotes: 3, downvotes: 1 };

describe("nextVote", () => {
  it("casts when there is no vote", () =>
    expect(nextVote(null, "UPVOTE")).toBe("UPVOTE"));
  it("removes when the same type is clicked", () =>
    expect(nextVote("UPVOTE", "UPVOTE")).toBeNull());
  it("switches when the other type is clicked", () =>
    expect(nextVote("UPVOTE", "DOWNVOTE")).toBe("DOWNVOTE"));
});

describe("applyVoteChange", () => {
  it("adds a new vote", () => {
    expect(applyVoteChange(counts, null, "UPVOTE")).toEqual({
      upvotes: 4,
      downvotes: 1,
    });
    expect(applyVoteChange(counts, null, "DOWNVOTE")).toEqual({
      upvotes: 3,
      downvotes: 2,
    });
  });
  it("removes an existing vote", () => {
    expect(applyVoteChange(counts, "UPVOTE", null)).toEqual({
      upvotes: 2,
      downvotes: 1,
    });
  });
  it("moves a vote between types", () => {
    expect(applyVoteChange(counts, "DOWNVOTE", "UPVOTE")).toEqual({
      upvotes: 4,
      downvotes: 0,
    });
  });
  it("never goes below zero and keeps other fields", () => {
    expect(
      applyVoteChange({ id: 9, upvotes: 0, downvotes: 0 }, "UPVOTE", null),
    ).toEqual({ id: 9, upvotes: 0, downvotes: 0 });
  });
});
