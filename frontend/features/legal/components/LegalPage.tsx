import type { ReactNode } from "react";

import { PageHeader } from "@/components/shared/PageHeader";

interface LegalPageProps {
  title: string;
  description?: string;
  children: ReactNode;
}

export function LegalPage({ title, description, children }: LegalPageProps) {
  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-10">
      <PageHeader title={title} description={description} />
      <div className="space-y-4 leading-7 [&_h2]:pt-4 [&_h2]:text-lg [&_h2]:font-semibold">
        {children}
      </div>
    </div>
  );
}
