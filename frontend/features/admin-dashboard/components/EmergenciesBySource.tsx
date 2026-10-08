"use client";

import { BarList } from "@/components/shared/BarList";
import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { ListSkeleton } from "@/components/shared/ListSkeleton";
import { SectionPanel } from "@/components/shared/SectionPanel";
import { useEmergencyCounts } from "@/features/admin-emergencies/hooks/useAdminEmergencies";
import { SOURCE_LABEL } from "@/features/emergencies/lib/source-labels";

export function EmergenciesBySource() {
  const { counts, isPending, error, refetch } = useEmergencyCounts();
  const total = counts.reduce((s, c) => s + c.count, 0);
  return (
    <SectionPanel title="Emergencies by trigger">
      {isPending ? (
        <ListSkeleton rows={5} />
      ) : error ? (
        <ErrorState message={error.message} onRetry={refetch} />
      ) : total === 0 ? (
        <EmptyState title="No emergencies recorded" />
      ) : (
        <BarList
          items={counts.map((c) => ({
            label: SOURCE_LABEL[c.source],
            value: c.count,
            color: "#c4161c",
          }))}
        />
      )}
    </SectionPanel>
  );
}
