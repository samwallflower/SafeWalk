"use client";

import { MenuIcon, PlusIcon } from "lucide-react";
import Link from "next/link";
import { useState } from "react";

import { Button, buttonVariants } from "@/components/ui/button";
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";

import { NavLinks } from "./NavLinks";

export function MobileNav() {
  const [open, setOpen] = useState(false);

  return (
    <Sheet open={open} onOpenChange={setOpen}>
      <SheetTrigger render={<Button variant="ghost" size="icon" aria-label="Open menu" className="md:hidden" />}>
        <MenuIcon />
      </SheetTrigger>
      <SheetContent side="left">
        <SheetHeader>
          <SheetTitle>SafeWalk</SheetTitle>
        </SheetHeader>
        <div className="flex flex-col gap-4 px-4">
          <NavLinks
            className="flex flex-col gap-1"
            linkClassName="rounded-lg px-2 py-2 text-sm font-medium hover:bg-muted"
            onNavigate={() => setOpen(false)}
          />
          <Link href="/report" onClick={() => setOpen(false)} className={buttonVariants({ size: "lg" })}>
            <PlusIcon /> Report Incident
          </Link>
        </div>
      </SheetContent>
    </Sheet>
  );
}
