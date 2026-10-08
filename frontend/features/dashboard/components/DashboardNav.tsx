"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import { cn } from "@/lib/utils";

const TABS = [
  { href: "/dashboard", label: "Overview" },
  { href: "/dashboard/reports", label: "My reports" },
  { href: "/dashboard/sessions", label: "Walk history" },
  { href: "/dashboard/contacts", label: "Contacts" },
  { href: "/dashboard/settings", label: "Settings" },
] as const;

export function DashboardNav() {
  const pathname = usePathname();
  return (
    <nav
      aria-label="Dashboard"
      className="flex gap-1 overflow-x-auto rounded-2xl bg-card p-1.5 shadow-sm ring-1 ring-foreground/5"
    >
      {TABS.map((tab) => {
        const active =
          tab.href === "/dashboard"
            ? pathname === tab.href
            : pathname.startsWith(tab.href);
        return (
          <Link
            key={tab.href}
            href={tab.href}
            aria-current={active ? "page" : undefined}
            className={cn(
              "rounded-xl px-3.5 py-2 text-sm font-semibold whitespace-nowrap transition",
              active
                ? "bg-muted text-primary"
                : "text-muted-foreground hover:text-foreground",
            )}
          >
            {tab.label}
          </Link>
        );
      })}
    </nav>
  );
}
