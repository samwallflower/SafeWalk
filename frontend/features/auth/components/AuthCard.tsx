import type { ReactNode } from "react";
import Image from "next/image";

interface AuthCardProps {
  title: string;
  description: string;
  children: ReactNode;
  footer?: ReactNode;
}

export function AuthCard({ title, description, children, footer }: AuthCardProps) {
  return (
    <div className="mx-auto w-full max-w-md px-4 py-10">
      <div className="rounded-2xl bg-card p-8 shadow-sm ring-1 ring-foreground/5">
        <div className="mb-6 flex flex-col items-center gap-3 text-center">
          <span className="rounded-2xl bg-info-soft p-2.5">
            <Image src="/safewalk-logo.png" alt="" width={44} height={44} className="rounded-xl" priority />
          </span>
          <h1 className="text-2xl font-bold tracking-tight">{title}</h1>
          <p className="text-sm leading-6 text-muted-foreground">{description}</p>
        </div>
        {children}
      </div>
      {footer ? <div className="mt-6 text-center text-sm text-muted-foreground">{footer}</div> : null}
    </div>
  );
}
