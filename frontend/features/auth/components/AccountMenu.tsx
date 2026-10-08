"use client";

import { LogOutIcon, UserIcon } from "lucide-react";
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
      <DropdownMenuTrigger render={<Button variant="default" size="icon-lg" className="rounded-full bg-brand" aria-label="Account menu" />}>
        <UserIcon />
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuGroup>
          <DropdownMenuLabel>{email}</DropdownMenuLabel>
        </DropdownMenuGroup>
        <DropdownMenuSeparator />
        <DropdownMenuItem render={<Link href={isAdmin ? "/admin" : "/dashboard"} />}>{isAdmin ? "Admin dashboard" : "My Safety"}</DropdownMenuItem>
        <DropdownMenuItem onClick={() => logout.mutate()}>
          <LogOutIcon /> Log out
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
