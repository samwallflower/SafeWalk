"use client";

import { MapIcon, TableIcon } from "lucide-react";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export type ExplorerView = "table" | "map";

interface ViewToggleProps {
  value: ExplorerView;
  onChange: (value: ExplorerView) => void;
}

export function ViewToggle({ value, onChange }: ViewToggleProps) {
  return (
    <div
      className="flex gap-1 rounded-xl bg-muted p-1"
      role="group"
      aria-label="View"
    >
      <Button
        type="button"
        variant="ghost"
        size="sm"
        aria-pressed={value === "table"}
        className={cn(value === "table" && "bg-card text-primary shadow-sm")}
        onClick={() => onChange("table")}
      >
        <TableIcon /> Table
      </Button>
      <Button
        type="button"
        variant="ghost"
        size="sm"
        aria-pressed={value === "map"}
        className={cn(value === "map" && "bg-card text-primary shadow-sm")}
        onClick={() => onChange("map")}
      >
        <MapIcon /> Map
      </Button>
    </div>
  );
}
