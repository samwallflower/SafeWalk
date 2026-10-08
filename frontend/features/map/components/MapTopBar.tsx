import type { ReactNode } from "react";

/** The white control bar across the top of the map (see Live Incident Map design). */
export function MapTopBar({ children }: { children: ReactNode }) {
  return (
    <div className="grid gap-2 rounded-2xl bg-card p-2 shadow-sm ring-1 ring-foreground/5 md:grid-cols-[minmax(0,18rem)_minmax(0,1fr)_auto] md:items-center">
      {children}
    </div>
  );
}
