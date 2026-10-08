import { PlusCircleIcon, ShieldIcon } from "lucide-react";
import Link from "next/link";

import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export function ReportCta() {
  return (
    <div className="hidden w-80 space-y-3 rounded-2xl bg-card p-4 shadow-sm ring-1 ring-foreground/5 md:block">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="font-bold">Notice something unsafe?</p>
          <p className="text-sm text-muted-foreground">Help fellow walkers navigate safely.</p>
        </div>
        <span className="rounded-full bg-info-soft p-2 text-primary">
          <ShieldIcon className="size-4" aria-hidden="true" />
        </span>
      </div>
      <Link href="/report" className={cn(buttonVariants({ size: "lg" }), "h-10 w-full font-semibold")}>
        <PlusCircleIcon /> Report an Incident
      </Link>
    </div>
  );
}
