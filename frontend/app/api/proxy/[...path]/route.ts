import { cookies } from "next/headers";
import { NextResponse } from "next/server";

import { TOKEN_COOKIE } from "@/lib/auth/cookie";
import { backendUrl } from "@/lib/server/backend";

/** Login must go through /api/auth/login so the JWT never reaches the browser. */
const BLOCKED_PATHS = new Set(["auth/login"]);

async function forward(request: Request, ctx: RouteContext<"/api/proxy/[...path]">) {
  const { path } = await ctx.params;
  if (path.some((s) => s === "." || s === ".." || s.includes("\\"))) {
    return NextResponse.json({ message: "Invalid path", data: null }, { status: 400 });
  }
  if (BLOCKED_PATHS.has(path.join("/"))) {
    return NextResponse.json({ message: "Not found", data: null }, { status: 404 });
  }

  const search = new URL(request.url).search;
  const headers = new Headers();
  const contentType = request.headers.get("content-type");
  if (contentType) headers.set("Content-Type", contentType);
  const token = (await cookies()).get(TOKEN_COOKIE)?.value;
  if (token) headers.set("Authorization", `Bearer ${token}`);

  const hasBody = request.method !== "GET" && request.method !== "HEAD";
  try {
    const upstream = await fetch(backendUrl(`/${path.map(encodeURIComponent).join("/")}${search}`), {
      method: request.method,
      headers,
      body: hasBody ? await request.text() : undefined,
      cache: "no-store",
    });
    return new Response(await upstream.text(), {
      status: upstream.status,
      headers: {
        "Content-Type": upstream.headers.get("content-type") ?? "application/json",
        "Cache-Control": "no-store",
      },
    });
  } catch {
    return NextResponse.json({ message: "Cannot reach the SafeWalk server", data: null }, { status: 502 });
  }
}

export { forward as GET, forward as POST, forward as PUT, forward as DELETE, forward as PATCH };
