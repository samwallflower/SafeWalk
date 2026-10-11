interface AreaChartProps {
  points: readonly { label: string; value: number }[];
  ariaLabel: string;
}

const W = 640;
const H = 220;
const PAD_TOP = 14;
const PAD_BOTTOM = 10;

/** Area chart: a forest-green line over a pale green fill, one dot per day. Pure SVG, no chart library. */
export function AreaChart({ points, ariaLabel }: AreaChartProps) {
  const max = Math.max(1, ...points.map((p) => p.value));
  const innerH = H - PAD_TOP - PAD_BOTTOM;
  const x = (i: number) => (points.length <= 1 ? W / 2 : (i / (points.length - 1)) * (W - 12) + 6);
  const y = (v: number) => PAD_TOP + innerH - (v / max) * innerH;
  const line = points.map((p, i) => `${i === 0 ? "M" : "L"}${x(i).toFixed(1)} ${y(p.value).toFixed(1)}`).join(" ");
  const area = points.length > 0 ? `${line} L${x(points.length - 1).toFixed(1)} ${H - PAD_BOTTOM} L${x(0).toFixed(1)} ${H - PAD_BOTTOM}Z` : "";
  // show at most ~7 labels so a 30 day window stays readable
  const step = Math.max(1, Math.ceil(points.length / 7));
  return (
    <div>
      <svg viewBox={`0 0 ${W} ${H}`} role="img" aria-label={ariaLabel} className="h-auto w-full">
        {[0, 1, 2, 3].map((g) => (
          <line key={g} x1="0" x2={W} y1={PAD_TOP + (innerH / 3) * g} y2={PAD_TOP + (innerH / 3) * g} stroke="var(--border)" strokeWidth="1" />
        ))}
        <path d={area} fill="var(--leaf)" opacity="0.55" />
        <path d={line} fill="none" stroke="var(--primary)" strokeWidth="3" strokeLinejoin="round" strokeLinecap="round" />
        {points.length <= 31
          ? points.map((p, i) => <circle key={`${p.label}-${i}`} cx={x(i)} cy={y(p.value)} r="4.5" fill="var(--primary)" stroke="var(--card)" strokeWidth="2.5" />)
          : null}
      </svg>
      <div className="mt-1 flex justify-between text-xs text-muted-foreground">
        {points.map((p, i) => (
          <span key={`${p.label}-${i}`} className={i % step === 0 ? "" : "invisible"}>
            {p.label}
          </span>
        ))}
      </div>
    </div>
  );
}
