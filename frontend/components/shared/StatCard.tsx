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
    <div className="space-y-2 rounded-2xl bg-card p-5 shadow-sm ring-1 ring-foreground/5">
      <div className="flex items-center justify-between">
        <p className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">{label}</p>
        <span className="rounded-lg bg-info-soft p-1.5 text-primary [&_svg]:size-4">{icon}</span>
      </div>
      {isLoading ? (
        <Skeleton className="h-9 w-20" aria-label={`Loading ${label}`} />
      ) : (
        <p className="text-3xl font-bold">{isError || value === null ? "—" : value}</p>
      )}
      <div className="min-h-5 text-sm text-muted-foreground">{isError ? "Could not load" : detail}</div>
    </div>
  );
}
