"use client";

import { ShieldCheckIcon } from "lucide-react";

import { Chip } from "@/components/shared/Chip";
import { Switch } from "@/components/ui/switch";

interface AnonymousSectionProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
}

export function AnonymousSection({ checked, onChange }: AnonymousSectionProps) {
  return (
    <section className="flex items-center justify-between gap-4 rounded-2xl bg-card p-5 shadow-sm ring-1 ring-foreground/5 md:p-6">
      <div className="space-y-1">
        <h2 className="flex items-center gap-2 text-lg font-bold">
          <ShieldCheckIcon className="size-5 text-primary" aria-hidden="true" />
          Post Anonymously
          <Chip tone="success">Recommended</Chip>
        </h2>
        <p className="text-sm text-muted-foreground">Your name won&apos;t be shown to others.</p>
      </div>
      <Switch checked={checked} onCheckedChange={onChange} aria-label="Post anonymously" />
    </section>
  );
}
