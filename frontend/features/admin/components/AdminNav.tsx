"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import { cn } from "@/lib/utils";

const TABS = [
  { href: "/admin", label: "Overview" },
  { href: "/admin/reports", label: "Reports" },
  { href: "/admin/incidents", label: "Moderation" },
  { href: "/admin/users", label: "Users" },
  { href: "/admin/categories", label: "Categories" },
  { href: "/admin/emergencies", label: "Emergencies" },
  { href: "/admin/sessions", label: "Sessions" },
  { href: "/admin/authorities", label: "Authorities" },
] as const;

export function AdminNav() {
  const pathname = usePathname();
  return (
    <nav aria-label="Admin" className="flex gap-1.5 overflow-x-auto rounded-xl border bg-card p-1.5">
      {TABS.map((tab) => {
        const active = tab.href === "/admin" ? pathname === tab.href : pathname.startsWith(tab.href);
        return (
          <Link
            key={tab.href}
            href={tab.href}
            aria-current={active ? "page" : undefined}
            className={cn(
              "rounded-lg px-4 py-2.5 text-sm font-semibold whitespace-nowrap transition",
              active ? "bg-leaf font-bold text-foreground" : "text-muted-foreground hover:text-foreground",
            )}
          >
            {tab.label}
          </Link>
        );
      })}
    </nav>
  );
}
