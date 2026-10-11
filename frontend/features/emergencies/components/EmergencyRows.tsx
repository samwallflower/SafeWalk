import { AlertTriangleIcon } from "lucide-react";

import { Chip } from "@/components/shared/Chip";
import { formatDateTime } from "@/lib/format";

import { SOURCE_LABEL } from "../lib/source-labels";
import type { Emergency } from "../types";

/** The emergencies of one walk as a short list. Shared by the walker's view and the admin's. */
export function EmergencyRows({ emergencies }: { emergencies: readonly Emergency[] }) {
  if (emergencies.length === 0) {
    return <p className="text-sm text-muted-foreground">No emergencies were triggered during this walk.</p>;
  }
  return (
    <ul className="space-y-2">
      {emergencies.map((e) => (
        <li key={e.id} className="flex flex-wrap items-center justify-between gap-2 rounded-lg bg-destructive-soft/60 p-3 text-sm">
          <span className="flex items-center gap-2 font-medium">
            <AlertTriangleIcon className="size-4 text-destructive" aria-hidden="true" />
            {SOURCE_LABEL[e.triggerSource]}
          </span>
          <span className="flex items-center gap-2 text-xs text-muted-foreground">
            {formatDateTime(e.triggerTimestamp)}
            <Chip tone={e.resolved ? "success" : "danger"}>{e.resolved ? "Resolved" : "Unresolved"}</Chip>
          </span>
        </li>
      ))}
    </ul>
  );
}
