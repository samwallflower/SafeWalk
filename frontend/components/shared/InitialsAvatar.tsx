import { cn } from "@/lib/utils";

interface InitialsAvatarProps {
  name: string;
  className?: string;
}

export function InitialsAvatar({ name, className }: InitialsAvatarProps) {
  const initials = name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
  return (
    <span
      aria-hidden="true"
      className={cn("flex size-10 shrink-0 items-center justify-center rounded-full bg-info-soft text-sm font-bold text-primary", className)}
    >
      {initials || "?"}
    </span>
  );
}
