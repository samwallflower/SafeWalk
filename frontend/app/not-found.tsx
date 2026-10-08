import Link from "next/link";

import { EmptyState } from "@/components/shared/EmptyState";
import { buttonVariants } from "@/components/ui/button";

export default function NotFound() {
  return (
    <div className="mx-auto w-full max-w-xl px-4 py-16">
      <EmptyState
        title="Page not found"
        description="The page you are looking for does not exist."
        action={
          <Link href="/" className={buttonVariants({ variant: "outline", size: "sm" })}>
            Back to home
          </Link>
        }
      />
    </div>
  );
}
