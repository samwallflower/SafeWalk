import type { Metadata } from "next";

import { SectionPanel } from "@/components/shared/SectionPanel";
import { SessionsList } from "@/features/sessions/components/SessionsList";

export const metadata: Metadata = { title: "Walk history — SafeWalk" };

export default function Page() {
  return (
    <SectionPanel title="Walk history">
      <SessionsList expandable />
    </SectionPanel>
  );
}
