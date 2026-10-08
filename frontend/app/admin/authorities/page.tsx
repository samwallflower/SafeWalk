import type { Metadata } from "next";

import { SectionPanel } from "@/components/shared/SectionPanel";
import { AuthoritiesManager } from "@/features/admin-authorities/components/AuthoritiesManager";

export const metadata: Metadata = { title: "Authorities — SafeWalk" };

export default function Page() {
  return (
    <SectionPanel title="Emergency authorities">
      <AuthoritiesManager />
    </SectionPanel>
  );
}
