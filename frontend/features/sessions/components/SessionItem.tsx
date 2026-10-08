"use client";

import { ArrowRightIcon, FootprintsIcon } from "lucide-react";
import { useState } from "react";

import { Button } from "@/components/ui/button";
import { EmergencyList } from "@/features/emergencies/components/EmergencyList";
import { useStreetName } from "@/hooks/useStreetName";
import { formatDateTime, formatDuration } from "@/lib/format";

import type { WalkSession } from "../types";
import { SessionStatusChip } from "./SessionStatusChip";

interface SessionItemProps {
  session: WalkSession;
  /** Dashboard preview hides the expandable emergency details. */
  expandable?: boolean;
}

export function SessionItem({ session, expandable = false }: SessionItemProps) {
  const [open, setOpen] = useState(false);
  const origin = useStreetName(session.originLatitude, session.originLongitude);
  const destination = useStreetName(session.destinationLatitude, session.destinationLongitude);
  const duration = formatDuration(session.startTime, session.endTime);

  return (
    <li className="space-y-3 rounded-xl bg-muted p-3">
      <div className="flex items-start gap-3">
        <span className="rounded-lg bg-card p-2 text-primary">
          <FootprintsIcon className="size-4" aria-hidden="true" />
        </span>
        <div className="min-w-0 flex-1">
          <p className="flex flex-wrap items-center gap-1.5 font-semibold">
            <span className="truncate">{origin.data ?? "Start"}</span>
            <ArrowRightIcon className="size-3.5 shrink-0 text-muted-foreground" aria-hidden="true" />
            <span className="truncate">{destination.data ?? "Destination"}</span>
          </p>
          <p className="text-xs text-muted-foreground">
            {formatDateTime(session.startTime)}
            {duration ? ` • ${duration}` : null}
          </p>
          {session.deviationTriggered || session.alarmTriggered ? (
            <p className="mt-1 text-xs text-muted-foreground">
              {[session.deviationTriggered ? "Route deviation noticed" : null, session.alarmTriggered ? "Safety check triggered" : null]
                .filter(Boolean)
                .join(" • ")}
            </p>
          ) : null}
        </div>
        <SessionStatusChip status={session.status} />
      </div>
      {expandable ? (
        <div className="space-y-2">
          <Button type="button" variant="ghost" size="xs" aria-expanded={open} onClick={() => setOpen((v) => !v)}>
            {open ? "Hide emergencies" : "Show emergencies"}
          </Button>
          {open ? <EmergencyList sessionId={session.id} /> : null}
        </div>
      ) : null}
    </li>
  );
}
