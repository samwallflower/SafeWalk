"use client";

import { ColumnChart } from "@/components/shared/ColumnChart";
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
        <ColumnChart
          highlightMax={false}
          ariaLabel="Emergencies by trigger"
          items={counts.map((c) => ({
            label: SOURCE_LABEL[c.source],
            value: c.count,
            color: "#e8806e",
          }))}
        />
      )}
    </SectionPanel>
  );
}
