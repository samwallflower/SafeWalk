import type { Metadata } from "next";
import Link from "next/link";
import { ArrowLeftIcon } from "lucide-react";

import { PageHeader } from "@/components/shared/PageHeader";
import { IncidentDetail } from "@/features/incidents/components/IncidentDetail";

export const metadata: Metadata = { title: "Incident — SafeWalk" };

export default async function IncidentPage({ params }: PageProps<"/incidents/[id]">) {
  const { id } = await params;
  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-8">
      <Link href="/map" className="mb-4 inline-flex items-center gap-1.5 text-sm font-medium hover:text-primary">
        <ArrowLeftIcon className="size-4" /> Back to Live Map
      </Link>
      <PageHeader title="Incident report" />
      <IncidentDetail id={Number(id)} />
    </div>
  );
}
