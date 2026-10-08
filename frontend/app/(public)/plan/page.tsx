import type { Metadata } from "next";

import { PlanScreen } from "@/features/routing/components/PlanScreen";

export const metadata: Metadata = { title: "Plan a safer walk — SafeWalk" };

export default function PlanPage() {
  return <PlanScreen />;
}
