"use client";

import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { AdminSessionEmergencies } from "@/features/admin-emergencies/components/AdminSessionEmergencies";
import { SessionStatusChip } from "@/features/sessions/components/SessionStatusChip";
import type { WalkSession } from "@/features/sessions/types";
import { useStreetName } from "@/hooks/useStreetName";
import { formatDateTime, formatDuration } from "@/lib/format";

interface SessionDrawerProps {
  session: WalkSession | null;
  onOpenChange: (open: boolean) => void;
}

/** Side drawer with everything about one walk session, including its emergencies. */
export function SessionDrawer({ session, onOpenChange }: SessionDrawerProps) {
  return (
    <Sheet open={session !== null} onOpenChange={onOpenChange}>
      <SheetContent side="right" className="w-full gap-0 overflow-y-auto sm:max-w-md">
        {session ? <SessionDetails session={session} /> : null}
      </SheetContent>
    </Sheet>
  );
}

function SessionDetails({ session }: { session: WalkSession }) {
  const origin = useStreetName(session.originLatitude, session.originLongitude);
  const destination = useStreetName(session.destinationLatitude, session.destinationLongitude);
  const duration = formatDuration(session.startTime, session.endTime);
  const flags = [
    session.deviationTriggered ? "Left the route" : null,
    session.alarmTriggered ? "Safety check triggered" : null,
    session.autoCompleted ? "Ended automatically on arrival" : null,
  ].filter((f): f is string => f !== null);

  const rows: readonly [string, string][] = [
    ["Walker", `User #${session.userId}`],
    ["Route", session.routeId === null ? "—" : `#${session.routeId}`],
    ["From", origin.data ?? "—"],
    ["To", destination.data ?? "—"],
    ["Started", formatDateTime(session.startTime)],
    ["Ended", session.endTime ? formatDateTime(session.endTime) : "Still going"],
    ["Duration", duration ?? "—"],
    ["Last seen", session.lastLocationUpdate ? formatDateTime(session.lastLocationUpdate) : "—"],
  ];

  return (
    <>
      <SheetHeader className="gap-2 p-6 pr-14">
        <SheetTitle className="text-2xl font-extrabold">Session #{session.id}</SheetTitle>
        <SheetDescription>Walk details and what the safety checks saw.</SheetDescription>
        <div>
          <SessionStatusChip status={session.status} />
        </div>
      </SheetHeader>
      <div className="space-y-5 px-6 pb-6">
        <dl className="divide-y rounded-xl border bg-card">
          {rows.map(([label, value]) => (
            <div key={label} className="flex items-start justify-between gap-4 p-3.5 text-sm">
              <dt className="text-muted-foreground">{label}</dt>
              <dd className="text-right font-semibold break-words">{value}</dd>
            </div>
          ))}
        </dl>
        {flags.length > 0 ? (
          <ul className="flex flex-wrap gap-2">
            {flags.map((f) => (
              <li key={f} className="rounded-lg bg-warning-soft px-3 py-1.5 text-xs font-bold text-warning">
                {f}
              </li>
            ))}
          </ul>
        ) : null}
        <section className="space-y-2">
          <h3 className="text-lg font-bold">Emergencies</h3>
          <AdminSessionEmergencies sessionId={session.id} />
        </section>
      </div>
    </>
  );
}
