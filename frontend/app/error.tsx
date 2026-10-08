"use client";

import { ErrorState } from "@/components/shared/ErrorState";

export default function GlobalError({ retry }: { error: Error; retry: () => void }) {
  return (
    <div className="mx-auto w-full max-w-xl px-4 py-16">
      <ErrorState message="An unexpected error occurred." onRetry={retry} />
    </div>
  );
}
