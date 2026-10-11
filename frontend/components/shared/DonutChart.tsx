export interface DonutSegment {
  label: string;
  value: number;
  color: string;
}

interface DonutChartProps {
  segments: readonly DonutSegment[];
  centreLabel: string;
}

const R = 60;
const CIRC = 2 * Math.PI * R;

/** Donut with the total in the middle and a legend beside it. Pure SVG, no chart library. */
export function DonutChart({ segments, centreLabel }: DonutChartProps) {
  const total = segments.reduce((s, x) => s + x.value, 0);
  const arcs = segments.map((s, i) => ({
    ...s,
    length: total === 0 ? 0 : (s.value / total) * CIRC,
    offset: total === 0 ? 0 : (segments.slice(0, i).reduce((sum, x) => sum + x.value, 0) / total) * CIRC,
  }));
  const summary = segments.map((s) => `${s.label} ${s.value}`).join(", ");
  return (
    <div className="flex flex-wrap items-center gap-6">
      <svg width="160" height="160" viewBox="0 0 160 160" role="img" aria-label={`${centreLabel}: ${summary}`}>
        <g fill="none" strokeWidth="22" transform="rotate(-90 80 80)">
          <circle cx="80" cy="80" r={R} stroke="var(--muted)" />
          {arcs.map((a) => (
            <circle key={a.label} cx="80" cy="80" r={R} stroke={a.color} strokeDasharray={`${a.length} ${CIRC}`} strokeDashoffset={-a.offset} />
          ))}
        </g>
        <text x="80" y="82" textAnchor="middle" className="fill-foreground font-heading text-2xl font-extrabold">
          {total.toLocaleString()}
        </text>
        <text x="80" y="100" textAnchor="middle" className="fill-muted-foreground text-xs">
          {centreLabel}
        </text>
      </svg>
      <ul className="space-y-2 text-sm">
        {segments.map((s) => (
          <li key={s.label} className="flex items-center gap-2">
            <span className="size-3 rounded-sm" style={{ backgroundColor: s.color }} aria-hidden="true" />
            <span>
              {s.label} <span className="text-muted-foreground">{total === 0 ? "0%" : `${Math.round((s.value / total) * 100)}%`}</span>
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}
