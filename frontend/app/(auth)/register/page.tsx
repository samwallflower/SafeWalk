import type { Metadata } from "next";
import Link from "next/link";
import { Suspense } from "react";

import { AuthCard } from "@/features/auth/components/AuthCard";
import { RegisterForm } from "@/features/auth/components/RegisterForm";

export const metadata: Metadata = { title: "Create your account — SafeWalk" };

export default function Page() {
  return (
    <AuthCard
      title="Create your account"
      description="Join the community to report incidents and vote to keep the map accurate."
      footer={<>Already registered? <Link href="/login" className="font-semibold text-primary">Sign in</Link></>}
    >
      <Suspense>
        <RegisterForm />
      </Suspense>
    </AuthCard>
  );
}
