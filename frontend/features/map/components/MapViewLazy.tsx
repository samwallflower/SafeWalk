"use client";

import dynamic from "next/dynamic";

import { Skeleton } from "@/components/ui/skeleton";

/** Mapbox needs the browser; keep it out of the server bundle. */
export const MapViewLazy = dynamic(() => import("./MapView").then((m) => m.MapView), {
  ssr: false,
  loading: () => <Skeleton className="h-full w-full rounded-none" />,
});
