import type { Metadata } from "next";

import { SectionPanel } from "@/components/shared/SectionPanel";
import { CategoriesManager } from "@/features/admin-categories/components/CategoriesManager";

export const metadata: Metadata = { title: "Categories — SafeWalk" };

export default function Page() {
  return (
    <SectionPanel title="Incident categories">
      <CategoriesManager />
    </SectionPanel>
  );
}
