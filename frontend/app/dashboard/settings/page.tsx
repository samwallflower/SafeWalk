import type { Metadata } from "next";

import { SettingsScreen } from "@/features/profile/components/SettingsScreen";

export const metadata: Metadata = { title: "Settings — SafeWalk" };

export default function SettingsPage() {
  return <SettingsScreen />;
}
