"use client";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

import type { StatsWindow } from "../lib/admin-stats";

const OPTIONS: { value: StatsWindow; label: string }[] = [
  { value: "24h", label: "24h" },
  { value: "7d", label: "Past 7 days" },
  { value: "30d", label: "30 days" },
];

interface WindowToggleProps {
  value: StatsWindow;
  onChange: (value: StatsWindow) => void;
}

export function WindowToggle({ value, onChange }: WindowToggleProps) {
  return (
    <div
      className="flex gap-1 rounded-xl bg-muted p-1"
      role="group"
      aria-label="Time window"
    >
      {OPTIONS.map((o) => (
        <Button
          key={o.value}
          type="button"
          variant="ghost"
          size="sm"
          aria-pressed={o.value === value}
          className={cn(o.value === value && "bg-card text-primary shadow-sm")}
          onClick={() => onChange(o.value)}
        >
          {o.label}
        </Button>
      ))}
    </div>
  );
}
