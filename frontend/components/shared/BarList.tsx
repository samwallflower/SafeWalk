export interface BarItem {
  label: string;
  value: number;
  color?: string;
}

interface BarListProps {
  items: readonly BarItem[];
  /** Denominator for the percentage; defaults to the sum of values. */
  total?: number;
}

/** Labelled horizontal bars (see Incident Breakdown by Category design). Pure CSS, no chart library. */
export function BarList({ items, total }: BarListProps) {
  const sum = total ?? items.reduce((s, i) => s + i.value, 0);
  return (
    <ul className="space-y-3">
      {items.map((item) => {
        const percent = sum > 0 ? Math.round((item.value / sum) * 100) : 0;
        return (
          <li key={item.label} className="space-y-1.5">
            <div className="flex items-center justify-between gap-3 text-sm">
              <span className="flex items-center gap-2 font-medium">
                <span className="size-2.5 rounded-full" style={{ backgroundColor: item.color ?? "var(--primary)" }} aria-hidden="true" />
                {item.label}
              </span>
              <span className="text-muted-foreground">
                {item.value.toLocaleString()} <span className="text-xs">({percent}%)</span>
              </span>
            </div>
            <div className="h-2 overflow-hidden rounded-full bg-muted" role="img" aria-label={`${item.label}: ${item.value} (${percent}%)`}>
              <div className="h-full rounded-full" style={{ width: `${percent}%`, backgroundColor: item.color ?? "var(--primary)" }} />
            </div>
          </li>
        );
      })}
    </ul>
  );
}
