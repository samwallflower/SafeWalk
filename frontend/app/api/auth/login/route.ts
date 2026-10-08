import { cookies } from "next/headers";
import { NextResponse } from "next/server";

import { TOKEN_COOKIE, tokenCookieOptions } from "@/lib/auth/cookie";
import { decodeToken } from "@/lib/auth/jwt";
import { isRecord } from "@/lib/http/envelope";
import { backendUrl } from "@/lib/server/backend";

function fail(status: number, message: string) {
  return NextResponse.json({ message, data: null }, { status });
}

export async function POST(request: Request) {
  const body: unknown = await request.json().catch(() => null);
  if (!isRecord(body)) return fail(400, "Invalid request body");

  let upstream: Response;
  try {
    upstream = await fetch(backendUrl("/auth/login"), {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: body.email, password: body.password }),
      cache: "no-store",
    });
  } catch {
    return fail(502, "Cannot reach the SafeWalk server");
  }

  const json: unknown = await upstream.json().catch(() => null);
  if (!upstream.ok) {
    const message = isRecord(json) && typeof json.message === "string" ? json.message : "Login failed";
    return fail(upstream.status, message);
  }

  const token = isRecord(json) && isRecord(json.data) ? json.data.token : undefined;
  const decoded = typeof token === "string" ? decodeToken(token) : null;
  if (typeof token !== "string" || !decoded) return fail(502, "Unexpected login response");

  (await cookies()).set(TOKEN_COOKIE, token, tokenCookieOptions(decoded.exp));
  return NextResponse.json({ message: "Login successful", data: decoded.user });
}
