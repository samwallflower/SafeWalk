import type { Metadata } from "next";

import { ModerationScreen } from "@/features/moderation/components/ModerationScreen";

export const metadata: Metadata = { title: "Moderation — SafeWalk" };

export default function Page() {
  return <ModerationScreen />;
}
