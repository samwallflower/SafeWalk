import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

const vote = vi.fn();
let canVote = false;
let myVote: "UPVOTE" | "DOWNVOTE" | null = null;

vi.mock("../hooks/useVote", () => ({
  useVote: () => ({ canVote, myVote, isPending: false, vote }),
}));

import { VoteButtons } from "./VoteButtons";

beforeEach(() => {
  vote.mockReset();
  canVote = false;
  myVote = null;
});

describe("VoteButtons", () => {
  it("is disabled with a login hint when signed out", () => {
    render(<VoteButtons reportId={1} upvotes={3} downvotes={1} />);
    const up = screen.getByRole("button", { name: "Upvote (3)" });
    expect(up).toBeDisabled();
    expect(up).toHaveAttribute("title", "Log in to vote");
  });

  it("casts the clicked vote type when signed in", async () => {
    canVote = true;
    render(<VoteButtons reportId={1} upvotes={3} downvotes={1} />);
    await userEvent.click(screen.getByRole("button", { name: "Downvote (1)" }));
    expect(vote).toHaveBeenCalledWith("DOWNVOTE");
  });

  it("marks the user's current vote as pressed", () => {
    canVote = true;
    myVote = "UPVOTE";
    render(<VoteButtons reportId={1} upvotes={4} downvotes={0} />);
    expect(screen.getByRole("button", { name: "Upvote (4)" })).toHaveAttribute(
      "aria-pressed",
      "true",
    );
    expect(
      screen.getByRole("button", { name: "Downvote (0)" }),
    ).toHaveAttribute("aria-pressed", "false");
  });
});
