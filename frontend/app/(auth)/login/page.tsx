import type { Metadata } from "next";
import Link from "next/link";
import { Suspense } from "react";

import { AuthCard } from "@/features/auth/components/AuthCard";
import { LoginForm } from "@/features/auth/components/LoginForm";

export const metadata: Metadata = { title: "Sign in — SafeWalk" };

export default function Page() {
  return (
    <AuthCard
      title="Sign in to SafeWalk"
      description="Sign in to report incidents and protect your neighbourhood. Your personal identity stays confidential on all live incident points."
      footer={<>Don&apos;t have an account? <Link href="/register" className="font-semibold text-primary">Sign up</Link></>}
    >
      <Suspense>
        <LoginForm />
      </Suspense>
    </AuthCard>
  );
}
