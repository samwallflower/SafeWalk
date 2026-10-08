import { UserRoundIcon } from "lucide-react";

import type { Incident } from "../types";

/** Anonymous reports never show a name; others show "First L." */
export function ReporterLine({ incident }: { incident: Pick<Incident, "isAnonymous" | "reporterName"> }) {
  const label = incident.isAnonymous ? "Anonymous walker" : incident.reporterName ? `Reported by ${incident.reporterName}` : null;
  if (!label) return null;
  return (
    <span className="flex items-center gap-1 text-xs text-muted-foreground">
      <UserRoundIcon className="size-3.5" aria-hidden="true" />
      {label}
    </span>
  );
}
