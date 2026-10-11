import type { Metadata } from "next";

import { SectionPanel } from "@/components/shared/SectionPanel";
import { MyReportsPaged } from "@/features/my-reports/components/MyReportsPaged";

export const metadata: Metadata = { title: "My reports — SafeWalk" };

export default function Page() {
  return (
    <SectionPanel title="My reports">
      <MyReportsPaged />
    </SectionPanel>
  );
}
