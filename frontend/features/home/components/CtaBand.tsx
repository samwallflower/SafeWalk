import { ListIcon, PlusIcon } from "lucide-react";
import Link from "next/link";

import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

import { Eyebrow } from "./Eyebrow";

export function CtaBand() {
  return (
    <section className="mx-auto w-full max-w-7xl px-4 py-20 md:px-8">
      <div className="flex flex-col items-start justify-between gap-8 border p-8 md:flex-row md:items-end md:p-12">
        <div className="max-w-2xl space-y-4">
          <Eyebrow>04 / Contribute</Eyebrow>
          <h2 className="text-4xl font-light tracking-tight text-balance">
            See something unsafe? Tell the neighbourhood.
          </h2>
          <p className="text-muted-foreground">
            A report takes under a minute and helps the next person choose a
            better route.
          </p>
        </div>
        <div className="flex flex-wrap gap-3">
          <Link
            href="/report"
            className={cn(
              buttonVariants({ size: "lg" }),
              "h-12 rounded-md px-6 text-xs font-semibold tracking-widest uppercase",
            )}
          >
            <PlusIcon /> Report an incident
          </Link>
          <Link
            href="/reports"
            className={cn(
              buttonVariants({ variant: "outline", size: "lg" }),
              "h-12 rounded-md bg-card px-6 text-xs font-semibold tracking-widest uppercase",
            )}
          >
            <ListIcon /> Browse reports
          </Link>
        </div>
      </div>
    </section>
  );
}
