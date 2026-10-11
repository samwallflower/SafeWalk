"use client";

import { ErrorState } from "@/components/shared/ErrorState";
import { Skeleton } from "@/components/ui/skeleton";

import { useSessionEmergencies } from "../hooks/useSessionEmergencies";
import { EmergencyRows } from "./EmergencyRows";

/** The signed-in walker's emergencies for one of their own sessions. */
export function EmergencyList({ sessionId }: { sessionId: number }) {
  const emergencies = useSessionEmergencies(sessionId, true);

  if (emergencies.isPending) return <Skeleton className="h-10 w-full" role="status" aria-label="Loading emergencies" />;
  if (emergencies.isError) {
    return <ErrorState message={emergencies.error.message} onRetry={() => void emergencies.refetch()} />;
  }
  return <EmergencyRows emergencies={emergencies.data} />;
}
