import Link from "next/link";

import { Logo } from "@/components/shared/Logo";

export function Footer() {
  return (
    <footer className="border-t bg-card">
      <div className="mx-auto flex max-w-7xl flex-col gap-4 px-4 py-3 text-sm text-muted-foreground md:flex-row md:items-center md:justify-between md:px-6">
        <Logo size={24} textClassName="text-base text-foreground" />
        <p className="max-w-xl text-xs leading-5">
          SafeWalk does not replace emergency services. In danger, call your local emergency number.
        </p>
        <nav aria-label="Legal" className="flex gap-6 font-semibold">
          <Link href="/about" className="hover:text-foreground">About</Link>
          <Link href="/privacy" className="hover:text-foreground">Privacy</Link>
          <Link href="/terms" className="hover:text-foreground">Terms</Link>
        </nav>
      </div>
    </footer>
  );
}
