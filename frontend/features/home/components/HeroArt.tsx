/** Decorative illustration of the product (map, red incident dots, a safer route). Not real data. */
export function HeroArt() {
  const dots: [number, number][] = [
    [90, 80], [140, 110], [118, 150], [210, 70], [250, 120], [300, 90], [330, 160], [190, 200], [260, 230], [120, 250], [360, 250], [60, 190],
  ];
  return (
    <svg viewBox="0 0 480 340" role="img" aria-label="Illustration of a map with incident dots and a safer route" className="h-auto w-full">
      <rect width="480" height="340" rx="24" fill="#eef2fa" />
      <g stroke="#ffffff" strokeLinecap="round">
        <path d="M0 110 H480" strokeWidth="16" />
        <path d="M0 230 H480" strokeWidth="12" />
        <path d="M110 0 V340" strokeWidth="14" />
        <path d="M280 0 V340" strokeWidth="16" />
        <path d="M400 0 V340" strokeWidth="10" />
        <path d="M0 40 L480 300" strokeWidth="8" />
      </g>
      <g fill="#dde5f3">
        <rect x="20" y="20" width="70" height="70" rx="10" />
        <rect x="130" y="130" width="130" height="80" rx="10" />
        <rect x="300" y="20" width="80" height="70" rx="10" />
        <rect x="300" y="250" width="80" height="70" rx="10" />
        <rect x="20" y="250" width="70" height="70" rx="10" />
      </g>
      {dots.map(([x, y]) => (
        <g key={`${x}-${y}`}>
          <circle cx={x} cy={y} r="14" fill="#c4161c" opacity="0.14" />
          <circle cx={x} cy={y} r="5" fill="#c4161c" stroke="#ffffff" strokeWidth="1.5" />
        </g>
      ))}
      <path d="M60 300 C110 270 120 200 190 170 S280 150 290 80 S380 50 420 40" fill="none" stroke="#ffffff" strokeWidth="11" strokeLinecap="round" />
      <path d="M60 300 C110 270 120 200 190 170 S280 150 290 80 S380 50 420 40" fill="none" stroke="#0a6b32" strokeWidth="6" strokeLinecap="round" />
      <circle cx="60" cy="300" r="9" fill="#0b52b8" stroke="#ffffff" strokeWidth="3" />
      <g transform="translate(420 40)">
        <circle r="16" fill="#c4161c" opacity="0.18" />
        <circle r="8" fill="#c4161c" stroke="#ffffff" strokeWidth="3" />
      </g>
      <g transform="translate(250 270)">
        <rect width="150" height="34" rx="17" fill="#ffffff" />
        <circle cx="19" cy="17" r="5" fill="#0a6b32" />
        <text x="34" y="22" fontSize="13" fontWeight="700" fill="#161a23">Safer route found</text>
      </g>
    </svg>
  );
}
