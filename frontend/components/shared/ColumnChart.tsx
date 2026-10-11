export interface ColumnItem {
  label: string;
  value: number;
  /** Overrides the default pastel bar colour. */
  color?: string;
}

interface ColumnChartProps {
  items: readonly ColumnItem[];
  ariaLabel: string;
  /** Picks the tallest column out in lantern yellow (the latest one if several tie). */
  highlightMax?: boolean;
  className?: string;
}

/** Column chart in pastel green. Zero days stay as a thin stub so the rhythm is still visible. */
export function ColumnChart({ items, ariaLabel, highlightMax = true, className }: ColumnChartProps) {
  const max = Math.max(0, ...items.map((i) => i.value));
  const busiest = highlightMax && max > 0 ? items.map((i) => i.value).lastIndexOf(max) : -1;
  return (
    <div role="img" aria-label={ariaLabel} className={className}>
      <div className="flex h-40 items-end justify-between gap-2 px-1">
        {items.map((item, i) => (
          <div key={`${item.label}-${i}`} className="flex h-full min-w-0 max-w-14 flex-1 flex-col justify-end gap-1.5" title={`${item.label}: ${item.value}`}>
            <span className="text-center text-xs font-bold text-foreground">{item.value > 0 ? item.value.toLocaleString() : ""}</span>
            <div
              className="w-full rounded-lg"
              style={{
                height: item.value === 0 ? 5 : `${Math.max(8, (item.value / max) * 85)}%`,
                backgroundColor: item.value === 0 ? "var(--border)" : (item.color ?? (i === busiest ? "var(--lantern)" : "var(--leaf)")),
              }}
            />
          </div>
        ))}
      </div>
      <div className="mt-2 flex justify-between gap-2 px-1 text-xs text-muted-foreground">
        {items.map((item, i) => (
          <span key={`${item.label}-${i}`} className="min-w-0 max-w-14 flex-1 truncate text-center">
            {item.label}
          </span>
        ))}
      </div>
    </div>
  );
}
