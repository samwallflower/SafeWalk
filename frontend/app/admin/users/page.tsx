import type { Metadata } from "next";

import { SectionPanel } from "@/components/shared/SectionPanel";
import { UsersManager } from "@/features/users/components/UsersManager";

export const metadata: Metadata = { title: "Users — SafeWalk" };

export default function Page() {
  return (
    <SectionPanel title="Users">
      <UsersManager />
    </SectionPanel>
  );
}
