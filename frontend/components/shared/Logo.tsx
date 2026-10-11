import Link from "next/link";

import { cn } from "@/lib/utils";

import { LanternPin } from "./LanternPin";

interface LogoProps {
  className?: string;
  textClassName?: string;
  size?: number;
}

export function Logo({ className, textClassName, size = 32 }: LogoProps) {
  return (
    <Link href="/" className={cn("flex items-center gap-2.5", className)}>
      <LanternPin size={size} />
      <span className={cn("font-heading text-xl font-extrabold tracking-tight", textClassName)}>SafeWalk</span>
    </Link>
  );
}
