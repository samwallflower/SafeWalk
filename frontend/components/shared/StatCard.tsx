import type { ReactNode } from "react";

import { Skeleton } from "@/components/ui/skeleton";

interface StatCardProps {
  label: string;
  value: string | null;
  isLoading: boolean;
  isError: boolean;
  detail?: ReactNode;
  icon: ReactNode;
}

/** Metric tile used on the dashboards (see My Safety Dashboard design). */
export function StatCard({ label, value, isLoading, isError, detail, icon }: StatCardProps) {
  return (
    <div className="space-y-2 rounded-xl border bg-card p-5">
      <div className="flex items-center justify-between">
        <p className="text-sm font-semibold text-muted-foreground">{label}</p>
        <span className="rounded-lg bg-accent p-2 text-primary [&_svg]:size-4">{icon}</span>
      </div>
      {isLoading ? (
        <Skeleton className="h-9 w-20" aria-label={`Loading ${label}`} />
      ) : (
        <p className="font-heading text-4xl leading-none font-extrabold">{isError || value === null ? "—" : value}</p>
      )}
      <div className="min-h-5 text-sm text-muted-foreground">{isError ? "Could not load" : detail}</div>
    </div>
  );
}
