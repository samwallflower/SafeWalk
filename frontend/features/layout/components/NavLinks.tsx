import Link from "next/link";

import { NAV_ITEMS } from "../nav-items";

interface NavLinksProps {
  className?: string;
  linkClassName?: string;
  onNavigate?: () => void;
}

export function NavLinks({ className, linkClassName, onNavigate }: NavLinksProps) {
  return (
    <nav aria-label="Main" className={className}>
      {NAV_ITEMS.map((item) => (
        <Link
          key={item.href}
          href={item.href}
          onClick={onNavigate}
          className={linkClassName}
        >
          {item.label}
        </Link>
      ))}
    </nav>
  );
}
