import { ClockIcon } from "lucide-react";
import Link from "next/link";

import { formatRelative } from "@/lib/format";

import type { Incident } from "../types";
import { CategoryBadge } from "./CategoryBadge";
import { IncidentStreet } from "./IncidentStreet";
import { ReporterLine } from "./ReporterLine";
import { VoteButtons } from "@/features/votes/components/VoteButtons";

const MAX_DESCRIPTION = 160;

export function IncidentSummary({ incident }: { incident: Incident }) {
  const description =
    incident.description.length > MAX_DESCRIPTION
      ? `${incident.description.slice(0, MAX_DESCRIPTION)}…`
      : incident.description;

  return (
    <div className="w-72 space-y-3 text-sm">
      <div className="space-y-1.5 pr-6">
        <div className="flex flex-wrap items-center gap-2">
          <CategoryBadge name={incident.category.name} />
          <IncidentStreet latitude={incident.latitude} longitude={incident.longitude} />
        </div>
        <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
          <ClockIcon className="size-3.5" aria-hidden="true" />
          Reported {formatRelative(incident.timestamp)}
        </p>
        <ReporterLine incident={incident} />
      </div>
      <p className="rounded-lg bg-info-soft p-3 leading-6 break-words">{description}</p>
      <div className="flex items-center justify-between gap-2">
        <VoteButtons reportId={incident.id} upvotes={incident.upvotes} downvotes={incident.downvotes} />
        <Link href={`/incidents/${incident.id}`} className="text-sm font-semibold text-primary hover:underline">
          View details
        </Link>
      </div>
    </div>
  );
}
