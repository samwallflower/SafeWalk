"use client";

import { ListIcon } from "lucide-react";
import Link from "next/link";
import { useState } from "react";

import { EmptyState } from "@/components/shared/EmptyState";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { CategoryBadge } from "@/features/incidents/components/CategoryBadge";
import type { Incident } from "@/features/incidents/types";
import { formatRelative } from "@/lib/format";

import { LIST_MAX_ITEMS } from "../config";

interface NearbyIncidentsListProps {
  incidents: readonly Incident[];
  zoomedIn: boolean;
}

/** Keyboard / screen-reader alternative to the map. */
export function NearbyIncidentsList({ incidents, zoomedIn }: NearbyIncidentsListProps) {
  const [open, setOpen] = useState(false);
  const shown = incidents.slice(0, LIST_MAX_ITEMS);

  return (
    <Sheet open={open} onOpenChange={setOpen}>
      <SheetTrigger render={<Button variant="secondary" size="sm" className="bg-muted" />}>
        <ListIcon /> List view
      </SheetTrigger>
      <SheetContent side="right" className="overflow-y-auto">
        <SheetHeader>
          <SheetTitle>Nearby incidents</SheetTitle>
          <SheetDescription>Newest first, within the visible map area.</SheetDescription>
        </SheetHeader>
        <div className="px-4 pb-4">
          {!zoomedIn ? (
            <EmptyState title="Zoom in to list incidents" description="The list is available at street level." />
          ) : shown.length === 0 ? (
            <EmptyState title="No incidents here" description="Try moving the map or clearing filters." />
          ) : (
            <ul className="space-y-3">
              {shown.map((incident) => (
                <li key={incident.id} className="space-y-2 rounded-xl bg-muted p-3 text-sm">
                  <div className="flex items-center justify-between gap-2">
                    <CategoryBadge name={incident.category.name} />
                    <span className="text-xs text-muted-foreground">{formatRelative(incident.timestamp)}</span>
                  </div>
                  <p className="line-clamp-2 break-words">{incident.description}</p>
                  <Link href={`/incidents/${incident.id}`} className="font-semibold text-primary hover:underline">
                    View details
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
}
