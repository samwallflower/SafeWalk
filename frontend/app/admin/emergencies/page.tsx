import type { Metadata } from "next";

import { SectionPanel } from "@/components/shared/SectionPanel";
import { EmergenciesTable } from "@/features/admin-emergencies/components/EmergenciesTable";

export const metadata: Metadata = { title: "Emergencies — SafeWalk" };

export default function Page() {
  return (
    <SectionPanel title="Emergencies">
      <EmergenciesTable />
    </SectionPanel>
  );
}
