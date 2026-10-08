"use client";

import { ArrowLeftIcon } from "lucide-react";
import Link from "next/link";

import { PageHeader } from "@/components/shared/PageHeader";
import { Skeleton } from "@/components/ui/skeleton";
import { useSession } from "@/features/auth/hooks/useSession";

import { ReportForm } from "./ReportForm";

export function ReportPageContent() {
  const { user, isLoading } = useSession();
  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-8">
      <Link href="/map" className="mb-4 inline-flex items-center gap-1.5 text-sm font-medium hover:text-primary">
        <ArrowLeftIcon className="size-4" /> Back to Live Map
      </Link>
      <PageHeader
        title="Report an Incident"
        description="Submit hazards or incidents to alert fellow walkers and recalculate community route safety."
      />
      {isLoading ? (
        <div className="space-y-4" role="status" aria-label="Loading">
          <Skeleton className="h-72 rounded-2xl" />
          <Skeleton className="h-48 rounded-2xl" />
        </div>
      ) : user ? (
        <ReportForm />
      ) : null}
    </div>
  );
}
