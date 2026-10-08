"use client";

import { ClockIcon } from "lucide-react";

import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { Skeleton } from "@/components/ui/skeleton";
import { useSession } from "@/features/auth/hooks/useSession";
import { formatDateTime } from "@/lib/format";
import { ApiError } from "@/lib/http/api-error";

import { useIncident } from "../hooks/useIncident";
import { CategoryBadge } from "./CategoryBadge";
import { IncidentStreet } from "./IncidentStreet";
import { ReporterLine } from "./ReporterLine";
import { IncidentLocationMapLazy } from "./IncidentLocationMapLazy";
import { StatusBadge } from "./StatusBadge";
import { VoteButtons } from "@/features/votes/components/VoteButtons";

export function IncidentDetail({ id }: { id: number }) {
  const incident = useIncident(id);
  const { isAdmin } = useSession();

  // A non-numeric id in the URL never starts a request, so it must not show a loading state forever.
  if (!Number.isInteger(id) || id <= 0) {
    return <EmptyState title="Incident not found" description="That link does not point to a report." />;
  }

  if (incident.isPending) {
    return (
      <div className="space-y-4" role="status" aria-label="Loading incident">
        <Skeleton className="h-6 w-1/3" />
        <Skeleton className="h-20 w-full" />
        <Skeleton className="h-64 w-full" />
      </div>
    );
  }

  if (incident.isError) {
    if (incident.error instanceof ApiError && incident.error.status === 404) {
      return <EmptyState title="Incident not found" description="It may have been removed." />;
    }
    return <ErrorState message={incident.error.message} onRetry={() => void incident.refetch()} />;
  }

  const data = incident.data;
  // Soft client-side check; the backend does not filter hidden reports on this endpoint.
  if (data.status !== "ACTIVE" && !isAdmin) {
    return <EmptyState title="Report unavailable" description="This report is not publicly visible." />;
  }

  return (
    <article className="space-y-5 rounded-2xl bg-card p-6 shadow-sm ring-1 ring-foreground/5">
      <div className="flex flex-wrap items-center gap-2">
        <CategoryBadge name={data.category.name} />
        {data.status !== "ACTIVE" ? <StatusBadge status={data.status} /> : null}
        <span className="flex items-center gap-1.5 text-sm text-muted-foreground">
          <ClockIcon className="size-4" aria-hidden="true" />
          {formatDateTime(data.timestamp)}
        </span>
        <IncidentStreet latitude={data.latitude} longitude={data.longitude} />
        <ReporterLine incident={data} />
      </div>
      <p className="rounded-lg bg-info-soft p-4 leading-7 break-words">{data.description}</p>
      <VoteButtons reportId={data.id} upvotes={data.upvotes} downvotes={data.downvotes} />
      <IncidentLocationMapLazy latitude={data.latitude} longitude={data.longitude} />
    </article>
  );
}
