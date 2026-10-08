import type { Metadata } from "next";

import { MapScreen } from "@/features/map/components/MapScreen";

export const metadata: Metadata = { title: "Live incident map — SafeWalk" };

export default function MapPage() {
  return <MapScreen />;
}
