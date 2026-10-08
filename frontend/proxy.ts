import { NextResponse, type NextRequest } from "next/server";

import { decideAccess } from "@/features/auth/route-guard";
import { TOKEN_COOKIE } from "@/lib/auth/cookie";
import { decodeToken, isExpired } from "@/lib/auth/jwt";

export function proxy(request: NextRequest) {
  const token = request.cookies.get(TOKEN_COOKIE)?.value;
  const decoded = token ? decodeToken(token) : null;
  const user = decoded && !isExpired(decoded) ? decoded.user : null;

  const { pathname, search } = request.nextUrl;
  const decision = decideAccess(pathname, search, user);
  if (decision.type === "redirect") {
    return NextResponse.redirect(new URL(decision.to, request.url));
  }
  return NextResponse.next();
}

export const config = {
  matcher: ["/dashboard/:path*", "/report/:path*", "/admin/:path*", "/login", "/register"],
};
