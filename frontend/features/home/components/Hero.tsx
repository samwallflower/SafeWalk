import { MapIcon, PlusIcon, RouteIcon, SearchIcon, ShieldCheckIcon } from "lucide-react";
import Link from "next/link";

import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

import { HeroArt } from "./HeroArt";

export function Hero() {
  return (
    <section className="mx-auto w-full max-w-7xl px-4 pt-10 pb-16 md:px-6">
      <div className="flex flex-col justify-between gap-8 pb-10 lg:flex-row lg:items-end">
        <div className="max-w-3xl space-y-5">
          <p className="inline-flex h-9 items-center gap-2 rounded-lg bg-warning-soft px-3.5 text-sm font-bold text-warning">
            <span className="size-2 rounded-full bg-lantern" aria-hidden="true" />
            Community-powered street safety
          </p>
          <h1 className="text-5xl leading-none font-extrabold text-balance md:text-7xl">
            Walk home like someone has your back.
          </h1>
        </div>
        <div className="max-w-md space-y-5">
          <p className="text-lg leading-8 text-muted-foreground">
            See what neighbours have reported, pick the calmer route, and start a walk. SafeWalk keeps watch and tells the people you trust if something looks wrong.
          </p>
          <div className="flex flex-wrap gap-3">
            <Link href="/map" className={cn(buttonVariants({ size: "lg" }), "h-14 px-7 text-base font-bold")}>
              <MapIcon /> Open the map
            </Link>
            <Link
              href="/plan"
              className={cn(
                buttonVariants({ variant: "outline", size: "lg" }),
                "h-14 border-primary px-7 text-base font-bold text-primary",
              )}
            >
              <RouteIcon /> Plan a safe route
            </Link>
          </div>
        </div>
      </div>

      <div className="relative h-[28rem] overflow-hidden rounded-3xl border bg-[#e8f8e5] md:h-[38rem]">
        <HeroArt />
        <div className="absolute top-6 left-6 flex h-14 w-[22.5rem] max-w-[calc(100%-3rem)] items-center gap-3 rounded-lg bg-card px-4 text-muted-foreground shadow-lg">
          <SearchIcon className="size-5" aria-hidden="true" /> Where to?
        </div>
        <div className="absolute bottom-6 left-6 hidden items-center gap-3.5 rounded-xl bg-card px-4 py-4 shadow-lg sm:flex">
          <span className="grid size-11 place-items-center rounded-lg bg-accent text-primary">
            <ShieldCheckIcon className="size-6" aria-hidden="true" />
          </span>
          <span>
            <span className="block font-bold">Calmer route found</span>
            <span className="block text-sm text-muted-foreground">Example only: 12 min, passes 3 recent reports</span>
          </span>
        </div>
        <Link
          href="/report"
          className={cn(
            buttonVariants({ size: "lg" }),
            "absolute right-6 bottom-6 h-14 bg-lantern px-6 text-lg font-bold text-foreground shadow-lg hover:bg-lantern/90",
          )}
        >
          <PlusIcon /> Report
        </Link>
      </div>
    </section>
  );
}
