import type { Metadata } from "next";

import { SectionPanel } from "@/components/shared/SectionPanel";
import { MyReportsList } from "@/features/my-reports/components/MyReportsList";

export const metadata: Metadata = { title: "My reports — SafeWalk" };

export default function Page() {
  return (
    <SectionPanel title="My reports">
      <MyReportsList />
    </SectionPanel>
  );
}
