"use client";

import { EyeIcon, EyeOffIcon, LockIcon } from "lucide-react";
import { useState, type ComponentProps } from "react";

import { IconInput } from "@/components/shared/IconInput";
import { Button } from "@/components/ui/button";

export function PasswordInput(props: Omit<ComponentProps<typeof IconInput>, "icon" | "trailing" | "type">) {
  const [visible, setVisible] = useState(false);
  return (
    <IconInput
      {...props}
      type={visible ? "text" : "password"}
      icon={<LockIcon />}
      trailing={
        <Button
          type="button"
          variant="ghost"
          size="icon-sm"
          aria-label={visible ? "Hide password" : "Show password"}
          onClick={() => setVisible((v) => !v)}
        >
          {visible ? <EyeOffIcon /> : <EyeIcon />}
        </Button>
      }
    />
  );
}
