"use client";

import { LayoutDashboardIcon, LogOutIcon, ShieldCheckIcon, UserIcon } from "lucide-react";
import Link from "next/link";

import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

import { useLogout } from "../hooks/useLogout";

interface AccountMenuProps {
  email: string;
  isAdmin: boolean;
}

export function AccountMenu({ email, isAdmin }: AccountMenuProps) {
  const logout = useLogout();
  return (
    <DropdownMenu>
      <DropdownMenuTrigger render={<Button variant="default" size="icon-lg" className="rounded-full bg-brand text-foreground" aria-label="Account menu" />}>
        <UserIcon />
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="min-w-60">
        <DropdownMenuGroup>
          <DropdownMenuLabel className="flex flex-col gap-0.5 py-2">
            <span className="truncate text-sm font-semibold text-foreground">{email}</span>
            <span className="text-xs font-normal text-muted-foreground">{isAdmin ? "Administrator" : "Signed in"}</span>
          </DropdownMenuLabel>
        </DropdownMenuGroup>
        <DropdownMenuSeparator />
        <DropdownMenuItem render={<Link href={isAdmin ? "/admin" : "/dashboard"} />}>
          {isAdmin ? <LayoutDashboardIcon /> : <ShieldCheckIcon />}
          {isAdmin ? "Admin panel" : "My Safety"}
        </DropdownMenuItem>
        <DropdownMenuItem onClick={() => logout.mutate()}>
          <LogOutIcon /> Log out
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
