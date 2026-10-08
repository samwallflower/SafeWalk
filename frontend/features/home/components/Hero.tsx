import { MapIcon, RouteIcon, ShieldCheckIcon } from "lucide-react";
import Link from "next/link";

import { Chip } from "@/components/shared/Chip";
import { buttonVariants } from "@/components/ui/button";

import { HeroArt } from "./HeroArt";
import { cn } from "@/lib/utils";

const POINTS = ["Report anonymously", "Free to use", "Works in any city"] as const;

export function Hero() {
  return (
    <section className="w-full overflow-hidden border-b bg-gradient-to-b from-info-soft via-background to-background">
      <div className="mx-auto grid max-w-7xl items-center gap-10 px-4 py-14 md:px-6 lg:grid-cols-[1.05fr_1fr] lg:py-20">
        <div className="space-y-6">
          <Chip tone="info" className="gap-1.5">
            <ShieldCheckIcon /> Community-powered safety map
          </Chip>
          <h1 className="text-4xl leading-tight font-extrabold tracking-tight text-balance md:text-6xl">
            Walk with <span className="text-primary">confidence</span>, in any city.
          </h1>
          <p className="max-w-xl text-lg leading-8 text-muted-foreground">
            SafeWalk turns reports from people on the ground into a live safety map, and suggests walking routes that steer
            clear of risky spots.
          </p>
          <div className="flex flex-wrap gap-3">
            <Link href="/map" className={cn(buttonVariants({ size: "lg" }), "h-12 px-6 text-base font-semibold")}>
              <MapIcon /> Explore the map
            </Link>
            <Link href="/plan" className={cn(buttonVariants({ variant: "secondary", size: "lg" }), "h-12 bg-card px-6 text-base font-semibold ring-1 ring-foreground/10")}>
              <RouteIcon /> Plan a safer route
            </Link>
          </div>
          <ul className="flex flex-wrap gap-x-6 gap-y-2 text-sm font-medium text-muted-foreground">
            {POINTS.map((p) => (
              <li key={p} className="flex items-center gap-1.5">
                <span className="size-1.5 rounded-full bg-success" aria-hidden="true" />
                {p}
              </li>
            ))}
          </ul>
        </div>
        <div className="rounded-3xl bg-card p-3 shadow-xl ring-1 ring-foreground/5">
          <HeroArt />
        </div>
      </div>
    </section>
  );
}
