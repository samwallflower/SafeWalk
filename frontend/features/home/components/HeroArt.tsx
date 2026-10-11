/** A street-grid map of an invented neighbourhood with a calmer route and some reports. An illustration, not live data. */
const ROUTE =
  "M130 400 H600 Q640 400 640 360 V340 Q640 300 680 300 H840 Q880 300 880 260 V170 Q880 130 920 130 H1060";

const REPORTS = [
  { x: 330, y: 300, c: "#cc4631" },
  { x: 352, y: 318, c: "#fbcb62" },
  { x: 310, y: 322, c: "#cc4631" },
  { x: 720, y: 400, c: "#fbcb62" },
  { x: 560, y: 215, c: "#cc4631" },
  { x: 950, y: 300, c: "#fbcb62" },
  { x: 200, y: 130, c: "#cc4631" },
  { x: 780, y: 215, c: "#fbcb62" },
  { x: 470, y: 350, c: "#fbcb62" },
  { x: 1000, y: 400, c: "#cc4631" },
  { x: 90, y: 215, c: "#fbcb62" },
] as const;

export function HeroArt() {
  return (
    <svg
      viewBox="0 0 1200 620"
      role="img"
      aria-label="Map of a neighbourhood: a single calmer walking route runs along the streets to a glowing lantern at the destination"
      className="absolute inset-0 h-full w-full"
      preserveAspectRatio="xMidYMid slice"
    >
      <rect width="1200" height="620" fill="#e8f8e5" />
      <path
        d="M0 470 C180 430 300 520 480 480 S760 430 900 470 1100 520 1200 490 V560 C1060 590 940 540 780 570 S500 600 340 560 120 590 0 550z"
        fill="#cfe6ee"
      />
      <rect x="640" y="60" width="200" height="150" rx="10" fill="#cbf1c6" />
      <rect x="110" y="250" width="130" height="110" rx="10" fill="#cbf1c6" />
      <rect x="960" y="250" width="170" height="130" rx="10" fill="#cbf1c6" />
      <path
        stroke="#fff"
        strokeWidth="14"
        fill="none"
        d="M0 130H1200M0 300H1200M0 400H1200M260 0V620M420 0V620M640 0V620M880 0V620M1060 0V620"
      />
      <path
        stroke="#fff"
        strokeWidth="6"
        fill="none"
        d="M0 60H1200M0 215H1200M0 350H1200M110 0V620M340 0V620M520 0V620M760 0V620M960 0V620M1140 0V620"
      />
      <path d={ROUTE} fill="none" stroke="#fff" strokeWidth="18" strokeLinecap="round" strokeLinejoin="round" />
      <path d={ROUTE} fill="none" stroke="#f2a81d" strokeWidth="9" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx="130" cy="400" r="11" fill="#1e6a1a" />
      <circle cx="130" cy="400" r="4" fill="#fff" />
      <circle cx="1060" cy="130" r="48" fill="#fbcb62" opacity="0.4" />
      <circle cx="1060" cy="130" r="26" fill="#fbcb62" />
      <circle cx="1060" cy="130" r="10" fill="#fff3d1" />
      <g stroke="#fff" strokeWidth="3.5">
        {REPORTS.map((r) => (
          <circle key={`${r.x}-${r.y}`} cx={r.x} cy={r.y} r="10" fill={r.c} />
        ))}
      </g>
    </svg>
  );
}
