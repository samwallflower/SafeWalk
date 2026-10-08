import Link from "next/link";

import { EmptyState } from "@/components/shared/EmptyState";
import { buttonVariants } from "@/components/ui/button";

export default function ForbiddenPage() {
  return (
    <div className="mx-auto w-full max-w-xl px-4 py-16">
      <EmptyState
        title="You don't have access"
        description="This area is restricted to administrators."
        action={
          <Link href="/" className={buttonVariants({ variant: "outline", size: "sm" })}>
            Back to home
          </Link>
        }
      />
    </div>
  );
}
