"use client";

import { Chip } from "@/components/shared/Chip";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { SOURCE_LABEL } from "@/features/emergencies/lib/source-labels";
import type { Emergency } from "@/features/emergencies/types";
import { useStreetName } from "@/hooks/useStreetName";
import { formatDateTime } from "@/lib/format";

import { useSetEmergencyResolved } from "../hooks/useAdminEmergencies";

interface EmergencyDrawerProps {
  emergency: Emergency | null;
  onOpenChange: (open: boolean) => void;
}

/** Side drawer with the details of one emergency, and a way to resolve or reopen it. */
export function EmergencyDrawer({ emergency, onOpenChange }: EmergencyDrawerProps) {
  return (
    <Sheet open={emergency !== null} onOpenChange={onOpenChange}>
      <SheetContent side="right" className="w-full gap-0 overflow-y-auto sm:max-w-md">
        {emergency ? <Details emergency={emergency} /> : null}
      </SheetContent>
    </Sheet>
  );
}

function Details({ emergency }: { emergency: Emergency }) {
  const street = useStreetName(emergency.triggerLatitude, emergency.triggerLongitude);
  const setResolved = useSetEmergencyResolved();
  const coords =
    emergency.triggerLatitude !== null && emergency.triggerLongitude !== null
      ? `${emergency.triggerLatitude.toFixed(5)}, ${emergency.triggerLongitude.toFixed(5)}`
      : "—";
  const rows: readonly [string, string][] = [
    ["Trigger", SOURCE_LABEL[emergency.triggerSource]],
    ["Raised", formatDateTime(emergency.triggerTimestamp)],
    ["Resolved", emergency.resolvedAt ? formatDateTime(emergency.resolvedAt) : "Not yet"],
    ["Where", street.data ?? "—"],
    ["Coordinates", coords],
    ["Session", emergency.walkSession ? `#${emergency.walkSession.id}` : "—"],
    ["Walker", emergency.walkSession ? `User #${emergency.walkSession.userId}` : "—"],
  ];
  return (
    <>
      <SheetHeader className="gap-2 p-6 pr-14">
        <SheetTitle className="text-2xl font-extrabold">Emergency #{emergency.id}</SheetTitle>
        <SheetDescription>What triggered it and where it happened.</SheetDescription>
        <div>
          <Chip tone={emergency.resolved ? "success" : "danger"}>{emergency.resolved ? "Resolved" : "Unresolved"}</Chip>
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
        <Button
          type="button"
          variant="outline"
          className="h-11 w-full border-primary text-primary"
          disabled={setResolved.isPending}
          onClick={() => setResolved.mutate({ id: emergency.id, resolved: !emergency.resolved })}
        >
          {emergency.resolved ? "Mark as unresolved" : "Mark as resolved"}
        </Button>
      </div>
    </>
  );
}
