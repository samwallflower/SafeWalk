import { cn } from "@/lib/utils";

/** Small monospaced label used above headings on the home page, e.g. "[ 01 / HOW IT WORKS ]". */
export function Eyebrow({
  children,
  className,
}: {
  children: string;
  className?: string;
}) {
  return (
    <p
      className={cn(
        "font-semibold text-[11px] tracking-[0.16em] text-muted-foreground uppercase",
        className,
      )}
    >
      [ {children} ]
    </p>
  );
}
