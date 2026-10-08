interface MiniBarsProps {
  points: readonly { label: string; value: number }[];
}

/** Small column chart: one bar per label, scaled to the tallest. */
export function MiniBars({ points }: MiniBarsProps) {
  const max = Math.max(1, ...points.map((p) => p.value));
  return (
    <div className="flex h-32 items-end gap-1.5" role="img" aria-label="Reports per day">
      {points.map((p) => (
        <div key={p.label} className="flex h-full min-w-0 max-w-14 flex-1 flex-col justify-end gap-1" title={`${p.label}: ${p.value}`}>
          <div className="w-full rounded-t-md bg-primary" style={{ height: `${Math.max(4, (p.value / max) * 100)}%` }} />
          <span className="truncate text-center text-[10px] text-muted-foreground">{p.label}</span>
        </div>
      ))}
    </div>
  );
}
