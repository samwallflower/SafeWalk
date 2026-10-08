import type { Metadata } from "next";

import { SectionPanel } from "@/components/shared/SectionPanel";
import { SessionsTable } from "@/features/admin-sessions/components/SessionsTable";

export const metadata: Metadata = { title: "Sessions — SafeWalk" };

export default function Page() {
  return (
    <SectionPanel title="Walk sessions">
      <SessionsTable />
    </SectionPanel>
  );
}
