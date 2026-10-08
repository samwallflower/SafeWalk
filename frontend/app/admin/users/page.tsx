import type { Metadata } from "next";

import { SectionPanel } from "@/components/shared/SectionPanel";
import { UsersTable } from "@/features/users/components/UsersTable";

export const metadata: Metadata = { title: "Users — SafeWalk" };

export default function Page() {
  return (
    <SectionPanel title="Users">
      <UsersTable />
    </SectionPanel>
  );
}
