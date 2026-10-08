"use client";

import dynamic from "next/dynamic";

import { Skeleton } from "@/components/ui/skeleton";

export const IncidentLocationMapLazy = dynamic(
  () => import("./IncidentLocationMap").then((m) => m.IncidentLocationMap),
  { ssr: false, loading: () => <Skeleton className="h-64 w-full" /> },
);
