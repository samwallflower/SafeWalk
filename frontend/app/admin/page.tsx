import type { Metadata } from "next";

import { AdminOverviewScreen } from "@/features/admin-dashboard/components/AdminOverviewScreen";

export const metadata: Metadata = { title: "Admin — SafeWalk" };

export default function AdminPage() {
  return <AdminOverviewScreen />;
}
