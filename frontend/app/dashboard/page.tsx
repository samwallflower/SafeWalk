import type { Metadata } from "next";

import { OverviewScreen } from "@/features/dashboard/components/OverviewScreen";

export const metadata: Metadata = { title: "My Safety — SafeWalk" };

export default function DashboardPage() {
  return <OverviewScreen />;
}
