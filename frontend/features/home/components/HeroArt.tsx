/** Schematic of the product (blocks, a reported spot, a safer route). An illustration, not live data. */
const MONO = { fontFamily: "var(--font-sans)", fontWeight: 600 } as const;

const BLOCKS = [
  { x: 150, y: 110, w: 130, h: 96, label: "BLOCK A" },
  { x: 330, y: 90, w: 150, h: 92, label: "PARK" },
  { x: 120, y: 280, w: 130, h: 96, label: "STATION" },
  { x: 330, y: 330, w: 150, h: 100, label: "RESIDENTIAL" },
  { x: 520, y: 200, w: 120, h: 130, label: "CAMPUS" },
] as const;

const SAFE: readonly (readonly [number, number])[] = [
  [110, 450],
  [250, 395],
  [305, 300],
  [440, 290],
  [520, 210],
  [610, 130],
];

export function HeroArt() {
  const safe = SAFE.map(([x, y]) => `${x},${y}`).join(" ");
  return (
    <svg
      viewBox="0 0 720 560"
      role="img"
      aria-label="Schematic map: a safer walking route that avoids a reported incident"
      className="h-full w-full"
      preserveAspectRatio="xMidYMid slice"
    >
      <defs>
        <pattern
          id="hero-grid"
          width="24"
          height="24"
          patternUnits="userSpaceOnUse"
        >
          <path
            d="M24 0H0V24"
            fill="none"
            stroke="#0b2a5c"
            strokeOpacity="0.06"
          />
        </pattern>
        <pattern
          id="hero-grid-major"
          width="120"
          height="120"
          patternUnits="userSpaceOnUse"
        >
          <path
            d="M120 0H0V120"
            fill="none"
            stroke="#0b2a5c"
            strokeOpacity="0.1"
          />
        </pattern>
        <radialGradient id="hero-glow" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="#0a6b32" stopOpacity="0.14" />
          <stop offset="100%" stopColor="#0a6b32" stopOpacity="0" />
        </radialGradient>
      </defs>

      <rect width="720" height="560" fill="url(#hero-grid)" />
      <rect width="720" height="560" fill="url(#hero-grid-major)" />

      <g
        fill="none"
        stroke="#0b2a5c"
        strokeOpacity="0.14"
        strokeDasharray="3 6"
      >
        <circle cx="360" cy="290" r="130" />
        <circle cx="360" cy="290" r="215" />
        <circle cx="360" cy="290" r="300" />
      </g>
      <g stroke="#0b2a5c" strokeOpacity="0.1">
        <path d="M360 0V560" />
        <path d="M0 290H720" />
      </g>

      {BLOCKS.map((b) => (
        <g key={b.label}>
          <rect
            x={b.x}
            y={b.y}
            width={b.w}
            height={b.h}
            fill="#ffffff"
            fillOpacity="0.7"
            stroke="#0b2a5c"
            strokeOpacity="0.16"
          />
          <text
            x={b.x + b.w / 2}
            y={b.y + b.h / 2 + 3}
            textAnchor="middle"
            fontSize="9"
            letterSpacing="1.2"
            fill="#0b2a5c"
            fillOpacity="0.4"
            style={MONO}
          >
            {b.label}
          </text>
        </g>
      ))}

      <circle cx="610" cy="130" r="90" fill="url(#hero-glow)" />

      <polyline
        points="250,395 330,470 470,440 540,330 520,210"
        fill="none"
        stroke="#c4161c"
        strokeOpacity="0.6"
        strokeWidth="1.5"
        strokeDasharray="4 5"
      />
      <g transform="translate(405 458)">
        <circle r="14" fill="#c4161c" fillOpacity="0.12" />
        <circle r="5" fill="#c4161c" stroke="#ffffff" strokeWidth="2" />
      </g>
      <text
        x="405"
        y="488"
        textAnchor="middle"
        fontSize="9"
        letterSpacing="1.2"
        fill="#c4161c"
        style={MONO}
      >
        REPORTED · AVOIDED
      </text>

      <polyline
        points={safe}
        fill="none"
        stroke="#0a6b32"
        strokeWidth="3"
        strokeLinejoin="round"
        strokeLinecap="round"
      />
      {SAFE.slice(1, -1).map(([x, y]) => (
        <circle
          key={`${x}-${y}`}
          cx={x}
          cy={y}
          r="5"
          fill="#ffffff"
          stroke="#0a6b32"
          strokeWidth="2"
        />
      ))}
      <circle
        cx="110"
        cy="450"
        r="7"
        fill="#ffffff"
        stroke="#0a6b32"
        strokeWidth="2.5"
      />
      <text
        x="110"
        y="476"
        textAnchor="middle"
        fontSize="9"
        letterSpacing="1.2"
        fill="#161a23"
        style={MONO}
      >
        START
      </text>
      <circle
        cx="610"
        cy="130"
        r="14"
        fill="none"
        stroke="#0a6b32"
        strokeOpacity="0.4"
      />
      <circle cx="610" cy="130" r="7" fill="#0a6b32" />
      <text
        x="610"
        y="106"
        textAnchor="middle"
        fontSize="9"
        letterSpacing="1.2"
        fill="#0a6b32"
        style={MONO}
      >
        DESTINATION
      </text>

      <g stroke="#0b2a5c" strokeOpacity="0.3">
        <path d="M30 40h12M36 34v12" />
        <path d="M678 40h12M684 34v12" />
        <path d="M30 520h12M36 514v12" />
        <path d="M678 520h12M684 514v12" />
      </g>
    </svg>
  );
}
