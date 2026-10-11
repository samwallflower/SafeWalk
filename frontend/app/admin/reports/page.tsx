import type { Metadata } from "next";

import { SectionPanel } from "@/components/shared/SectionPanel";
import { AdminReportsTable } from "@/features/admin-reports/components/AdminReportsTable";

export const metadata: Metadata = { title: "Reports — SafeWalk" };

export default function Page() {
  return (
    <SectionPanel title="Reports">
      <AdminReportsTable />
    </SectionPanel>
  );
}
