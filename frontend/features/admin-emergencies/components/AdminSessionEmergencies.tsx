"use client";

import { ErrorState } from "@/components/shared/ErrorState";
import { Skeleton } from "@/components/ui/skeleton";
import { EmergencyRows } from "@/features/emergencies/components/EmergencyRows";

import { useSessionEmergenciesAdmin } from "../hooks/useAdminEmergencies";

/** Emergencies of any walk session, for admins (the walker's own list is checked against the signed-in user). */
export function AdminSessionEmergencies({ sessionId }: { sessionId: number }) {
  const emergencies = useSessionEmergenciesAdmin(sessionId);
  if (emergencies.isPending) return <Skeleton className="h-10 w-full" role="status" aria-label="Loading emergencies" />;
  if (emergencies.isError) return <ErrorState message={emergencies.error.message} onRetry={() => void emergencies.refetch()} />;
  return <EmergencyRows emergencies={emergencies.data} />;
}
