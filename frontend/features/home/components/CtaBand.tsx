import { ListIcon, PlusIcon } from "lucide-react";
import Link from "next/link";

import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export function CtaBand() {
  return (
    <section className="mx-auto w-full max-w-7xl space-y-5 px-4 py-20 md:px-6">
      <h2 className="max-w-2xl text-4xl leading-[1.05] font-extrabold text-balance md:text-5xl">
        See something unsafe? Tell the neighbourhood.
      </h2>
      <p className="max-w-xl text-lg text-muted-foreground">
        A report takes under a minute and helps the next person choose a better route.
      </p>
      <div className="flex flex-wrap gap-3">
        <Link
          href="/report"
          className={cn(buttonVariants({ size: "lg" }), "h-14 bg-lantern px-7 text-base font-bold text-foreground hover:bg-lantern/90")}
        >
          <PlusIcon /> Report an incident
        </Link>
        <Link
          href="/reports"
          className={cn(
            buttonVariants({ variant: "outline", size: "lg" }),
            "h-14 border-primary px-7 text-base font-bold text-primary",
          )}
        >
          <ListIcon /> Browse reports
        </Link>
      </div>
    </section>
  );
}
