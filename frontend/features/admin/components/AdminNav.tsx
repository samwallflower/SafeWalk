"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import { cn } from "@/lib/utils";

const TABS = [
  { href: "/admin", label: "Overview" },
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
    <nav aria-label="Admin" className="flex gap-1 overflow-x-auto rounded-2xl bg-card p-1.5 shadow-sm ring-1 ring-foreground/5">
      {TABS.map((tab) => {
        const active = tab.href === "/admin" ? pathname === tab.href : pathname.startsWith(tab.href);
        return (
          <Link
            key={tab.href}
            href={tab.href}
            aria-current={active ? "page" : undefined}
            className={cn(
              "rounded-xl px-3.5 py-2 text-sm font-semibold whitespace-nowrap transition",
              active ? "bg-muted text-primary" : "text-muted-foreground hover:text-foreground",
            )}
          >
            {tab.label}
          </Link>
        );
      })}
    </nav>
  );
}
