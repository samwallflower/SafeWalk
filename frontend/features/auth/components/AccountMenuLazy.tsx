"use client";

import dynamic from "next/dynamic";

import { Skeleton } from "@/components/ui/skeleton";

/** The dropdown code is only needed once a user is signed in, so guests never download it. */
export const AccountMenuLazy = dynamic(() => import("./AccountMenu").then((m) => m.AccountMenu), {
  ssr: false,
  loading: () => <Skeleton className="size-9 rounded-full" aria-label="Loading account menu" />,
});
