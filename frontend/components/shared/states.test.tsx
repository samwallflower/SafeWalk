import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import { EmptyState } from "./EmptyState";
import { ErrorState } from "./ErrorState";
import { ListSkeleton } from "./ListSkeleton";

describe("shared states", () => {
  it("renders the empty state", () => {
    render(<EmptyState title="Nothing here" description="Add one" />);
    expect(screen.getByText("Nothing here")).toBeInTheDocument();
  });

  it("calls onRetry from the error state", async () => {
    const onRetry = vi.fn();
    render(<ErrorState message="Boom" onRetry={onRetry} />);
    await userEvent.click(screen.getByRole("button", { name: "Try again" }));
    expect(onRetry).toHaveBeenCalledOnce();
  });

  it("renders skeleton rows as a loading status", () => {
    render(<ListSkeleton rows={3} />);
    expect(screen.getByRole("status", { name: "Loading" })).toBeInTheDocument();
  });
});
