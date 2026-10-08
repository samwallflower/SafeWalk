import { Chip, type ChipTone } from "@/components/shared/Chip";

import type { SessionStatus } from "../types";

const STATUS: Record<SessionStatus, { label: string; tone: ChipTone }> = {
  COMPLETED: { label: "Completed", tone: "success" },
  ACTIVE: { label: "In progress", tone: "info" },
  EMERGENCY: { label: "Emergency", tone: "danger" },
  ABANDONED: { label: "Abandoned", tone: "neutral" },
};

export function SessionStatusChip({ status }: { status: SessionStatus }) {
  const { label, tone } = STATUS[status];
  return <Chip tone={tone}>{label}</Chip>;
}
