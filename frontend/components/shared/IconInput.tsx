import type { ComponentProps, ReactNode } from "react";

import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";

interface IconInputProps extends ComponentProps<typeof Input> {
  icon: ReactNode;
  trailing?: ReactNode;
}

/** shadcn Input with a leading icon and optional trailing control, in the tinted style from the designs. */
export function IconInput({ icon, trailing, className, ...props }: IconInputProps) {
  return (
    <div className="relative">
      <span className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-muted-foreground [&_svg]:size-4">
        {icon}
      </span>
      <Input className={cn("h-11 border-transparent bg-muted pl-9", trailing ? "pr-10" : null, className)} {...props} />
      {trailing ? <span className="absolute top-1/2 right-2 -translate-y-1/2">{trailing}</span> : null}
    </div>
  );
}
