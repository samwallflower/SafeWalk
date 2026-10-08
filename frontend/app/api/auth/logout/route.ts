import { cookies } from "next/headers";
import { NextResponse } from "next/server";

import { TOKEN_COOKIE } from "@/lib/auth/cookie";

export async function POST() {
  (await cookies()).delete(TOKEN_COOKIE);
  return NextResponse.json({ message: "Logged out", data: null });
}
