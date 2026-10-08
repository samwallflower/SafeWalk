import type { ReactNode } from "react";

import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

export type ChipTone = "success" | "danger" | "info" | "neutral";

const TONES: Record<ChipTone, string> = {
  success: "bg-success-soft text-success",
  danger: "bg-destructive-soft text-destructive",
  info: "bg-info-soft text-primary",
  neutral: "bg-muted text-muted-foreground",
};

interface ChipProps {
  tone?: ChipTone;
  className?: string;
  children: ReactNode;
}

/** Soft tinted pill used for categories, statuses and counters (see designs). */
export function Chip({ tone = "neutral", className, children }: ChipProps) {
  return (
    <Badge variant="secondary" className={cn("h-6 rounded-md px-2 font-semibold", TONES[tone], className)}>
      {children}
    </Badge>
  );
}
