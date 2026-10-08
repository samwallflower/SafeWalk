import { PlusIcon } from "lucide-react";
import Link from "next/link";

import { Logo } from "@/components/shared/Logo";
import { buttonVariants } from "@/components/ui/button";
import { UserMenu } from "@/features/auth/components/UserMenu";

import { MobileNav } from "./MobileNav";
import { NavLinks } from "./NavLinks";
import { cn } from "@/lib/utils";

export function Header() {
  return (
    <header className="sticky top-0 z-40 border-b bg-card">
      <div className="mx-auto flex h-16 max-w-7xl items-center gap-3 px-4 md:px-6">
        <MobileNav />
        <Logo />
        <NavLinks
          className="ml-8 hidden items-center gap-6 md:flex"
          linkClassName="text-sm font-medium text-foreground/80 hover:text-primary"
        />
        <div className="ml-auto flex items-center gap-2">
          <Link href="/report" className={cn(buttonVariants({ size: "lg" }), "hidden h-9 px-4 font-semibold sm:inline-flex")}>
            <PlusIcon /> Report Incident
          </Link>
          <UserMenu />
        </div>
      </div>
    </header>
  );
}
