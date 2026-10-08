import { Chip, type ChipTone } from "@/components/shared/Chip";
import type { ReportStatus } from "@/features/incidents/types";

const STATUS: Record<ReportStatus, { label: string; tone: ChipTone }> = {
  ACTIVE: { label: "Active on map", tone: "danger" },
  UNDER_REVIEW: { label: "Under review", tone: "info" },
  HIDDEN: { label: "Hidden", tone: "neutral" },
};

export function ReportStatusChip({ status }: { status: ReportStatus }) {
  const { label, tone } = STATUS[status];
  return <Chip tone={tone}>{label}</Chip>;
}
