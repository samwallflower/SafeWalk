import type { Metadata } from "next";
import Link from "next/link";
import { Suspense } from "react";

import { AuthCard } from "@/features/auth/components/AuthCard";
import { VerifyCodeForm } from "@/features/auth/components/VerifyCodeForm";

export const metadata: Metadata = { title: "Verify your email — SafeWalk" };

export default function Page() {
  return (
    <AuthCard
      title="Verify your email"
      description="Enter the code we sent to your email to activate your account."
      footer={<>Back to <Link href="/login" className="font-semibold text-primary">Sign in</Link></>}
    >
      <Suspense>
        <VerifyCodeForm />
      </Suspense>
    </AuthCard>
  );
}
