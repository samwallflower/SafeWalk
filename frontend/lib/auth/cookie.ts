export const TOKEN_COOKIE = "safewalk_token";

export function tokenCookieOptions(expSeconds: number) {
  return {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax" as const,
    path: "/",
    expires: new Date(expSeconds * 1000),
  };
}
