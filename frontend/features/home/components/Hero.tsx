import { MapIcon, RouteIcon } from "lucide-react";
import Link from "next/link";

import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

import { Eyebrow } from "./Eyebrow";
import { HeroArt } from "./HeroArt";

const TAGS = [
  "Anonymous reporting",
  "Free to use",
  "Works in any city",
] as const;

export function Hero() {
  return (
    <section className="grid w-full border-b bg-card lg:grid-cols-[1fr_1.1fr]">
      <div className="flex flex-col justify-center gap-8 px-4 py-14 md:px-8 lg:py-24 xl:pr-16 xl:pl-[max(2rem,calc((100vw-80rem)/2+2rem))]">
        <Eyebrow>01 / Safer walking</Eyebrow>
        <h1 className="text-5xl leading-[1.02] font-light tracking-tight text-balance md:text-7xl">
          Walk with <span className="font-normal">confidence</span>
          <span className="text-success">.</span>
        </h1>
        <p className="max-w-xl text-lg leading-8 text-muted-foreground">
          SafeWalk turns reports from people on the ground into a live safety
          map, and suggests walking routes that steer clear of the spots people
          have flagged.
        </p>
        <ul className="flex flex-wrap gap-2">
          {TAGS.map((tag) => (
            <li
              key={tag}
              className="border px-2.5 py-1 font-semibold text-[11px] tracking-wider text-muted-foreground uppercase"
            >
              {tag}
            </li>
          ))}
        </ul>
        <div className="flex flex-wrap gap-3 border-t pt-8">
          <Link
            href="/map"
            className={cn(
              buttonVariants({ size: "lg" }),
              "h-12 rounded-md px-6 text-xs font-semibold tracking-widest uppercase",
            )}
          >
            <MapIcon /> Explore the map
          </Link>
          <Link
            href="/plan"
            className={cn(
              buttonVariants({ variant: "outline", size: "lg" }),
              "h-12 rounded-md bg-card px-6 text-xs font-semibold tracking-widest uppercase",
            )}
          >
            <RouteIcon /> Plan a safer route
          </Link>
        </div>
      </div>

      <div className="relative min-h-80 border-t bg-background lg:min-h-[34rem] lg:border-t-0 lg:border-l">
        <HeroArt />
        <p className="absolute top-4 left-4 font-semibold text-[10px] tracking-[0.16em] text-muted-foreground uppercase">
          <span
            className="mr-2 inline-block size-1.5 bg-success align-middle"
            aria-hidden="true"
          />
          Route preview
        </p>
        <p className="absolute right-4 bottom-4 font-semibold text-[10px] tracking-[0.16em] text-muted-foreground uppercase">
          Illustration · not live data
        </p>
      </div>
    </section>
  );
}
