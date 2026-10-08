"use client";

import type { ComponentProps } from "react";

import { Textarea } from "@/components/ui/textarea";

import { DESCRIPTION_MAX } from "../schemas/report-schema";
import { SectionCard } from "./SectionCard";

interface DescriptionSectionProps extends Omit<ComponentProps<typeof Textarea>, "value"> {
  length: number;
  fieldError?: string;
}

export function DescriptionSection({ length, fieldError, ...props }: DescriptionSectionProps) {
  return (
    <SectionCard step={3} title="Description Narrative" hint={`${length} / ${DESCRIPTION_MAX}`}>
      <p className="text-sm text-muted-foreground">
        Keep it factual and concise. Avoid sharing personally identifiable details of bystanders.
      </p>
      <Textarea
        rows={4}
        aria-label="Description"
        aria-invalid={!!fieldError}
        placeholder="Describe what you observed (e.g. streetlights out on the eastern side, group blocking the walkway…)"
        className="min-h-28 border-transparent bg-muted"
        {...props}
      />
      {fieldError ? (
        <p role="alert" className="text-sm text-destructive">
          {fieldError}
        </p>
      ) : null}
    </SectionCard>
  );
}
