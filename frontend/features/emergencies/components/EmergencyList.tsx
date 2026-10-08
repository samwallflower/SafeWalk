"use client";

import { AlertTriangleIcon } from "lucide-react";

import { Chip } from "@/components/shared/Chip";
import { ErrorState } from "@/components/shared/ErrorState";
import { Skeleton } from "@/components/ui/skeleton";
import { formatDateTime } from "@/lib/format";

import { useSessionEmergencies } from "../hooks/useSessionEmergencies";
import { SOURCE_LABEL } from "../lib/source-labels";

export function EmergencyList({ sessionId }: { sessionId: number }) {
  const emergencies = useSessionEmergencies(sessionId, true);

  if (emergencies.isPending) return <Skeleton className="h-10 w-full" role="status" aria-label="Loading emergencies" />;
  if (emergencies.isError) {
    return <ErrorState message={emergencies.error.message} onRetry={() => void emergencies.refetch()} />;
  }
  if (emergencies.data.length === 0) {
    return <p className="text-sm text-muted-foreground">No emergencies were triggered during this walk.</p>;
  }
  return (
    <ul className="space-y-2">
      {emergencies.data.map((e) => (
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
