import Link from "next/link";
import type { ReactNode } from "react";

interface SectionPanelProps {
  title: string;
  icon?: ReactNode;
  action?: { href: string; label: string };
  headerExtra?: ReactNode;
  children: ReactNode;
}

/** White card with a title row, used for dashboard sections. */
export function SectionPanel({ title, icon, action, headerExtra, children }: SectionPanelProps) {
  return (
    <section className="space-y-4 rounded-2xl bg-card p-5 shadow-sm ring-1 ring-foreground/5">
      <header className="flex items-center justify-between gap-3">
        <h2 className="flex items-center gap-2 text-lg font-bold [&_svg]:size-5 [&_svg]:text-primary">
          {icon}
          {title}
        </h2>
        <div className="flex items-center gap-3">
          {headerExtra}
          {action ? (
            <Link href={action.href} className="text-sm font-semibold text-primary hover:underline">
              {action.label}
            </Link>
          ) : null}
        </div>
      </header>
      {children}
    </section>
  );
}
