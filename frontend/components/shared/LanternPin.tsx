/** The SafeWalk mark: a forest-green pin holding a lantern. */
export function LanternPin({ size = 32, className }: { size?: number; className?: string }) {
  return (
    <svg width={size} height={size} viewBox="0 0 48 48" aria-hidden="true" className={className}>
      <path
        d="M24 3C14.6 3 7 10.4 7 19.6 7 31.8 24 45 24 45s17-13.2 17-25.4C41 10.4 33.4 3 24 3z"
        fill="#1e6a1a"
      />
      <circle cx="24" cy="19.5" r="9" fill="#fbcb62" />
      <circle cx="24" cy="19.5" r="4" fill="#fff3d1" />
    </svg>
  );
}
