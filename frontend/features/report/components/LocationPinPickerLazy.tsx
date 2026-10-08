"use client";

import dynamic from "next/dynamic";

import { Skeleton } from "@/components/ui/skeleton";

export const LocationPinPickerLazy = dynamic(() => import("./LocationPinPicker").then((m) => m.LocationPinPicker), {
  ssr: false,
  loading: () => <Skeleton className="h-56 w-full md:h-64" />,
});
