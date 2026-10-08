import { cookies } from "next/headers";
import { NextResponse } from "next/server";

import { TOKEN_COOKIE } from "@/lib/auth/cookie";
import { decodeToken, isExpired } from "@/lib/auth/jwt";

/** Returns the current user from the cookie, or `data: null` when signed out. */
export async function GET() {
  const token = (await cookies()).get(TOKEN_COOKIE)?.value;
  const decoded = token ? decodeToken(token) : null;
  const user = decoded && !isExpired(decoded) ? decoded.user : null;
  return NextResponse.json({ message: "Session", data: user }, { headers: { "Cache-Control": "no-store" } });
}
