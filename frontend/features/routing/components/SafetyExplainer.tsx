"use client";

import { ChevronDownIcon } from "lucide-react";
import { useState } from "react";

import { cn } from "@/lib/utils";

export function SafetyExplainer() {
  const [open, setOpen] = useState(false);
  return (
    <div className="rounded-2xl bg-card p-4 text-sm shadow-sm ring-1 ring-foreground/5">
      <button
        type="button"
        className="flex w-full items-center justify-between gap-2 font-semibold"
        aria-expanded={open}
        onClick={() => setOpen((v) => !v)}
      >
        How routes are ranked
        <ChevronDownIcon className={cn("size-4 transition", open && "rotate-180")} aria-hidden="true" />
      </button>
      {open ? (
        <p className="mt-3 text-muted-foreground">
          Routes are ranked by how safe they are overall, based on the severity of the incidents reported near them. The
          recommended route may be slightly longer, but it passes fewer risky spots.
        </p>
      ) : null}
    </div>
  );
}
