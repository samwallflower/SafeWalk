"use client";

import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { Skeleton } from "@/components/ui/skeleton";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import type { IncidentCategory } from "@/features/categories/types";
import { cn } from "@/lib/utils";

import { SectionCard } from "./SectionCard";

interface CategorySectionProps {
  categories: readonly IncidentCategory[] | undefined;
  isLoading: boolean;
  error: Error | null;
  onRetry: () => void;
  value: number | undefined;
  onChange: (id: number) => void;
  fieldError?: string;
}

export function CategorySection({ categories, isLoading, error, onRetry, value, onChange, fieldError }: CategorySectionProps) {
  return (
    <SectionCard step={2} title="Category of Hazard" hint="Select one">
      <p className="text-sm text-muted-foreground">Pick the option that best describes what you saw.</p>
      {isLoading ? (
        <div className="grid gap-3 md:grid-cols-2" role="status" aria-label="Loading categories">
          {Array.from({ length: 4 }, (_, i) => (
            <Skeleton key={i} className="h-20 rounded-xl" />
          ))}
        </div>
      ) : error ? (
        <ErrorState message={error.message} onRetry={onRetry} />
      ) : !categories || categories.length === 0 ? (
        <EmptyState title="No categories available" description="An administrator needs to add incident categories." />
      ) : (
        <RadioGroup
          value={value === undefined ? "" : String(value)}
          onValueChange={(v) => onChange(Number(v))}
          className="grid gap-3 md:grid-cols-2"
          aria-label="Category of hazard"
        >
          {categories.map((category) => (
            <label
              key={category.id}
              className={cn(
                "flex cursor-pointer items-start gap-3 rounded-xl bg-muted p-3 ring-1 ring-transparent transition",
                "has-[[aria-checked=true]]:bg-info-soft has-[[aria-checked=true]]:ring-primary",
              )}
            >
              <RadioGroupItem value={String(category.id)} className="mt-0.5" />
              <span className="space-y-0.5 text-sm">
                <span className="block font-semibold">{category.name}</span>
                {category.description ? <span className="block text-muted-foreground">{category.description}</span> : null}
              </span>
            </label>
          ))}
        </RadioGroup>
      )}
      {fieldError ? (
        <p role="alert" className="text-sm text-destructive">
          {fieldError}
        </p>
      ) : null}
    </SectionCard>
  );
}
