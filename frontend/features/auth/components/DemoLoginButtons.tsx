"use client";

import { Button } from "@/components/ui/button";
import { env } from "@/lib/env";

import type { LoginValues } from "../schemas/login-schema";

interface DemoLoginButtonsProps {
  onFill: (values: LoginValues) => void;
}

/** Fills the form only; never submits. Hidden unless demo login is enabled. */
export function DemoLoginButtons({ onFill }: DemoLoginButtonsProps) {
  if (!env.demoLoginEnabled) return null;
  return (
    <div className="space-y-2">
      <div className="flex items-center gap-3 text-xs font-medium tracking-wide text-muted-foreground uppercase">
        <span className="h-px flex-1 bg-border" />
        Demo accounts for evaluation
        <span className="h-px flex-1 bg-border" />
      </div>
      <div className="grid grid-cols-2 gap-2">
        <Button type="button" variant="secondary" className="h-10" onClick={() => onFill(env.demoUser)}>
          Use demo user
        </Button>
        <Button type="button" variant="secondary" className="h-10" onClick={() => onFill(env.demoAdmin)}>
          Use demo admin
        </Button>
      </div>
    </div>
  );
}
