"use client";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export type ExplorerScope = "area" | "all";

const OPTIONS: readonly { value: ExplorerScope; label: string }[] = [
  { value: "area", label: "Around a place" },
  { value: "all", label: "All reports" },
];

export function ScopeToggle({
  value,
  onChange,
}: {
  value: ExplorerScope;
  onChange: (value: ExplorerScope) => void;
}) {
  return (
    <div
      className="flex gap-1 rounded-xl bg-muted p-1"
      role="group"
      aria-label="Scope"
    >
      {OPTIONS.map((o) => (
        <Button
          key={o.value}
          type="button"
          variant="ghost"
          size="sm"
          aria-pressed={value === o.value}
          className={cn(value === o.value && "bg-card text-primary shadow-sm")}
          onClick={() => onChange(o.value)}
        >
          {o.label}
        </Button>
      ))}
    </div>
  );
}
