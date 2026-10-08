import type { ReactNode } from "react";

import { DashboardNav } from "@/features/dashboard/components/DashboardNav";

export default function DashboardLayout({ children }: { children: ReactNode }) {
  return (
    <div className="mx-auto w-full max-w-7xl space-y-6 px-4 py-8 md:px-6">
      <DashboardNav />
      {children}
    </div>
  );
}
