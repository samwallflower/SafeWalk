import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import { DataTable, type Column } from "./DataTable";

interface Row {
  id: number;
  name: string;
}

const columns: readonly Column<Row>[] = [{ key: "name", header: "Name", cell: (r) => r.name }];

function setup(overrides: Partial<Parameters<typeof DataTable<Row>>[0]> = {}) {
  const onRetry = vi.fn();
  render(
    <DataTable
      columns={columns}
      rows={[{ id: 1, name: "Alpha" }]}
      getKey={(r) => r.id}
      isLoading={false}
      error={null}
      onRetry={onRetry}
      emptyTitle="Nothing here"
      {...overrides}
    />,
  );
  return { onRetry };
}

describe("DataTable states", () => {
  it("renders rows", () => {
    setup();
    expect(screen.getByText("Alpha")).toBeInTheDocument();
    expect(screen.getByRole("columnheader", { name: "Name" })).toBeInTheDocument();
  });

  it("shows a loading status while loading", () => {
    setup({ isLoading: true, rows: undefined });
    expect(screen.getByRole("status", { name: "Loading" })).toBeInTheDocument();
  });

  it("shows the empty state", () => {
    setup({ rows: [] });
    expect(screen.getByText("Nothing here")).toBeInTheDocument();
  });

  it("shows the error and lets the user retry", async () => {
    const { onRetry } = setup({ error: new Error("Boom"), rows: undefined });
    expect(screen.getByRole("alert")).toHaveTextContent("Boom");
    await userEvent.click(screen.getByRole("button", { name: "Try again" }));
    expect(onRetry).toHaveBeenCalledOnce();
  });
});
