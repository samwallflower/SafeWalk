"use client";

import { InitialsAvatar } from "@/components/shared/InitialsAvatar";
import { DataTable, type Column } from "@/components/shared/DataTable";
import type { UserDto } from "@/features/auth/types";

import { useAllUsers } from "../hooks/useAllUsers";

const COLUMNS: readonly Column<UserDto>[] = [
  {
    key: "user",
    header: "User",
    cell: (u) => (
      <span className="flex items-center gap-3">
        <InitialsAvatar name={`${u.firstName} ${u.lastName}`} className="size-8 text-xs" />
        <span className="font-semibold">{`${u.firstName} ${u.lastName}`.trim() || "—"}</span>
      </span>
    ),
  },
  { key: "email", header: "Email", cell: (u) => u.email },
  { key: "phone", header: "Phone", cell: (u) => u.phoneNumber ?? "—" },
  { key: "id", header: "ID", cell: (u) => `#${u.id}`, className: "text-muted-foreground" },
];

export function UsersTable() {
  const users = useAllUsers();
  return (
    <DataTable
      columns={COLUMNS}
      rows={users.data}
      getKey={(u) => u.id}
      isLoading={users.isPending}
      error={users.error}
      onRetry={() => void users.refetch()}
      emptyTitle="No users yet"
    />
  );
}
