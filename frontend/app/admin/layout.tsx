import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import type { ReactNode } from "react";

import { AdminNav } from "@/features/admin/components/AdminNav";
import { TOKEN_COOKIE } from "@/lib/auth/cookie";
import { decodeToken, isExpired } from "@/lib/auth/jwt";
import { isAdmin } from "@/lib/auth/session-types";

/** Second layer behind proxy.ts: server-side role check for the whole /admin tree. */
export default async function AdminLayout({ children }: { children: ReactNode }) {
  const token = (await cookies()).get(TOKEN_COOKIE)?.value;
  const decoded = token ? decodeToken(token) : null;
  if (!decoded || isExpired(decoded)) redirect("/login?redirect=%2Fadmin");
  if (!isAdmin(decoded.user)) redirect("/forbidden");
  return (
    <div className="mx-auto w-full max-w-7xl space-y-6 px-4 py-8 md:px-6">
      <AdminNav />
      {children}
    </div>
  );
}
