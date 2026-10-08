import Image from "next/image";
import Link from "next/link";

import { cn } from "@/lib/utils";

interface LogoProps {
  className?: string;
  textClassName?: string;
  size?: number;
}

export function Logo({ className, textClassName, size = 32 }: LogoProps) {
  return (
    <Link href="/" className={cn("flex items-center gap-2.5", className)}>
      <Image src="/safewalk-logo.png" alt="" width={size} height={size} className="rounded-lg" priority />
      <span className={cn("text-lg font-bold tracking-tight", textClassName)}>SafeWalk</span>
    </Link>
  );
}
