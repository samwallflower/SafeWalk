"use client";

import dynamic from "next/dynamic";

import { Skeleton } from "@/components/ui/skeleton";

export const RoutesMapLazy = dynamic(() => import("./RoutesMap").then((m) => m.RoutesMap), {
  ssr: false,
  loading: () => <Skeleton className="h-full w-full rounded-none" />,
});
