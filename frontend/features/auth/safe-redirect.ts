/** Only same-site relative paths are allowed as post-login destinations. */
export function safeRedirect(target: string | null | undefined): string | null {
  if (!target || !target.startsWith("/") || target.startsWith("//") || target.includes("\\")) return null;
  return target;
}
