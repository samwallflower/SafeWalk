import type { ReactNode } from "react";

interface MapSummaryProps {
  count: number;
  zoomedOut: boolean;
  action: ReactNode;
}

/** Bottom-left status card: how many incidents are drawn, plus the list-view action. */
export function MapSummary({ count, zoomedOut, action }: MapSummaryProps) {
  return (
    <div className="flex items-center gap-3 rounded-2xl bg-card px-3 py-2 text-sm shadow-sm ring-1 ring-foreground/5">
      <span className="flex items-center gap-2 font-semibold">
        <span className="size-2.5 rounded-full bg-destructive" aria-hidden="true" />
        {zoomedOut ? "Zoom in to see incidents" : `${count.toLocaleString()} incidents in view`}
      </span>
      <span className="h-5 w-px bg-border" aria-hidden="true" />
      {action}
    </div>
  );
}
