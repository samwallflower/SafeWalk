import { Chip } from "@/components/shared/Chip";

import type { ReportStatus } from "../types";

const LABELS: Record<ReportStatus, string> = {
  ACTIVE: "Active",
  HIDDEN: "Hidden",
  UNDER_REVIEW: "Under review",
};

export function StatusBadge({ status }: { status: ReportStatus }) {
  return <Chip tone={status === "ACTIVE" ? "success" : "neutral"}>{LABELS[status]}</Chip>;
}
