import type { Metadata } from "next";

import { ReportPageContent } from "@/features/report/components/ReportPageContent";

export const metadata: Metadata = { title: "Report an incident — SafeWalk" };

export default function ReportPage() {
  return <ReportPageContent />;
}
