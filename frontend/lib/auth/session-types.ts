export type Role = "ROLE_USER" | "ROLE_ADMIN";

export interface SessionUser {
  id: number;
  email: string;
  roles: string[];
}

export function isAdmin(user: Pick<SessionUser, "roles">): boolean {
  return user.roles.includes("ROLE_ADMIN");
}
