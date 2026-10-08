import { ListIcon, PlusIcon } from "lucide-react";
import Link from "next/link";

import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export function CtaBand() {
  return (
    <section className="mx-auto w-full max-w-7xl px-4 py-16 md:px-6">
      <div className="flex flex-col items-start justify-between gap-6 rounded-3xl bg-primary p-8 text-primary-foreground md:flex-row md:items-center md:p-12">
        <div className="max-w-xl space-y-2">
          <h2 className="text-3xl font-bold tracking-tight">See something unsafe? Tell the neighbourhood.</h2>
          <p className="text-primary-foreground/80">A report takes under a minute and helps the next person choose a better route.</p>
        </div>
        <div className="flex flex-wrap gap-3">
          <Link href="/report" className={cn(buttonVariants({ variant: "secondary", size: "lg" }), "h-12 px-6 font-semibold text-primary")}>
            <PlusIcon /> Report an incident
          </Link>
          <Link href="/reports" className="inline-flex h-12 items-center gap-2 rounded-lg px-6 font-semibold ring-1 ring-primary-foreground/40 hover:bg-primary-foreground/10">
            <ListIcon className="size-4" /> Browse reports
          </Link>
        </div>
      </div>
    </section>
  );
}
