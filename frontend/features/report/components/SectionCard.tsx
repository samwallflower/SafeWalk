import type { ReactNode } from "react";

interface SectionCardProps {
  step?: number;
  title: string;
  hint?: string;
  children: ReactNode;
}

/** White numbered card used for each part of the report form (see Report Incident design). */
export function SectionCard({ step, title, hint, children }: SectionCardProps) {
  return (
    <section className="space-y-4 rounded-2xl bg-card p-5 shadow-sm ring-1 ring-foreground/5 md:p-6">
      <header className="flex items-center justify-between gap-3">
        <h2 className="flex items-center gap-3 text-lg font-bold">
          {step ? (
            <span className="flex size-7 items-center justify-center rounded-full bg-muted text-sm font-semibold">{step}</span>
          ) : null}
          {title}
        </h2>
        {hint ? <span className="text-xs text-muted-foreground">{hint}</span> : null}
      </header>
      {children}
    </section>
  );
}
