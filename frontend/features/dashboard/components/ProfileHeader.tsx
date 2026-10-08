"use client";

import { MailIcon, PlusIcon, RouteIcon } from "lucide-react";
import Link from "next/link";

import { Chip } from "@/components/shared/Chip";
import { InitialsAvatar } from "@/components/shared/InitialsAvatar";
import { buttonVariants } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { useSession } from "@/features/auth/hooks/useSession";
import { useProfile } from "@/features/profile/hooks/useProfile";
import { cn } from "@/lib/utils";

export function ProfileHeader() {
  const { user } = useSession();
  const profile = useProfile();
  const name = profile.data
    ? `${profile.data.firstName} ${profile.data.lastName}`.trim()
    : null;

  return (
    <section className="flex flex-wrap items-center justify-between gap-4 rounded-2xl bg-card p-5 shadow-sm ring-1 ring-foreground/5">
      <div className="flex items-center gap-4">
        <InitialsAvatar
          name={name ?? user?.email ?? ""}
          className="size-16 text-xl"
        />
        <div className="space-y-1">
          {profile.isPending ? (
            <Skeleton className="h-7 w-48" aria-label="Loading name" />
          ) : (
            <h1 className="flex flex-wrap items-center gap-2 text-2xl font-bold tracking-tight">
              {name || user?.email}
              <Chip tone="success">Verified Walker</Chip>
            </h1>
          )}
          <p className="flex items-center gap-1.5 text-sm text-muted-foreground">
            <MailIcon className="size-4" aria-hidden="true" />
            {user?.email}
          </p>
        </div>
      </div>
      <div className="flex flex-wrap gap-2">
        <Link
          href="/report"
          className={cn(buttonVariants({ size: "lg" }), "font-semibold")}
        >
          <PlusIcon /> Report an Incident
        </Link>
        <Link
          href="/plan"
          className={cn(
            buttonVariants({ variant: "secondary", size: "lg" }),
            "font-semibold",
          )}
        >
          <RouteIcon /> Plan a route
        </Link>
      </div>
    </section>
  );
}
