import type { Metadata } from "next";

import { ExplorerScreen } from "@/features/reports-explorer/components/ExplorerScreen";

export const metadata: Metadata = { title: "Incident reports — SafeWalk" };

export default function ReportsPage() {
  return <ExplorerScreen />;
}
