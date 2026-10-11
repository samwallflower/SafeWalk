"use client";

import Link from "next/link";

import { buttonVariants } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

import { useSession } from "../hooks/useSession";
import { AccountMenuLazy } from "./AccountMenuLazy";

export function UserMenu() {
  const { user, isAdmin, isLoading } = useSession();

  if (isLoading) return <Skeleton className="h-9 w-28" aria-label="Loading account" />;

  if (!user) {
    return (
      <>
        <Link href="/login" className={cn(buttonVariants({ variant: "ghost" }), "hidden sm:inline-flex")}>
          Sign In
        </Link>
        <Link href="/register" className={cn(buttonVariants({ variant: "outline" }), "bg-card")}>
          Create Account
        </Link>
      </>
    );
  }

  return (
    <AccountMenuLazy email={user.email} isAdmin={isAdmin} />
  );
}
