import Link from "next/link";

import { Logo } from "@/components/shared/Logo";

export function Footer() {
  return (
    <footer className="border-t bg-card">
      <div className="mx-auto flex max-w-7xl flex-col gap-4 px-4 py-6 text-sm text-muted-foreground md:flex-row md:items-center md:justify-between md:px-6">
        <div className="flex items-center gap-3">
          <Logo size={24} textClassName="text-base text-foreground" />
          <span>© 2026 SafeWalk</span>
        </div>
        <nav aria-label="Legal" className="flex gap-6">
          <Link href="/about" className="hover:text-foreground">About</Link>
          <Link href="/privacy" className="hover:text-foreground">Privacy</Link>
          <Link href="/terms" className="hover:text-foreground">Terms</Link>
        </nav>
      </div>
    </footer>
  );
}
