import type { ReactNode } from "react";

import { LanternPin } from "@/components/shared/LanternPin";

interface AuthCardProps {
  title: string;
  description: string;
  children: ReactNode;
  footer?: ReactNode;
}

export function AuthCard({ title, description, children, footer }: AuthCardProps) {
  return (
    <div className="mx-auto w-full max-w-md px-4 py-10">
      <div className="rounded-xl border bg-card p-8">
        <div className="mb-6 flex flex-col items-center gap-3 text-center">
          <span className="rounded-xl bg-accent p-3">
            <LanternPin size={44} />
          </span>
          <h1 className="text-3xl font-extrabold">{title}</h1>
          <p className="text-sm leading-6 text-muted-foreground">{description}</p>
        </div>
        {children}
      </div>
      {footer ? <div className="mt-6 text-center text-sm text-muted-foreground">{footer}</div> : null}
    </div>
  );
}
