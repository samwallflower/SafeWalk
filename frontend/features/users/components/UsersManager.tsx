"use client";

import { PencilIcon, PlusIcon, Trash2Icon } from "lucide-react";
import { useState } from "react";

import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { InitialsAvatar } from "@/components/shared/InitialsAvatar";
import { Button } from "@/components/ui/button";
import type { UserDto } from "@/features/auth/types";

import { useAllUsers } from "../hooks/useAllUsers";
import { useDeleteUser } from "../hooks/useUserMutations";
import { UserDialog } from "./UserDialog";

type Editing = { mode: "closed" } | { mode: "add" } | { mode: "edit"; user: UserDto };

const fullName = (u: UserDto) => `${u.firstName} ${u.lastName}`.trim();

export function UsersManager() {
  const users = useAllUsers();
  const remove = useDeleteUser();
  const [editing, setEditing] = useState<Editing>({ mode: "closed" });
  const [deleting, setDeleting] = useState<UserDto | null>(null);

  const columns: readonly Column<UserDto>[] = [
    {
      key: "user",
      header: "User",
      cell: (u) => (
        <span className="flex items-center gap-3">
          <InitialsAvatar name={fullName(u)} className="size-9 text-xs" />
          <span className="font-semibold">{fullName(u) || "—"}</span>
        </span>
      ),
    },
    { key: "email", header: "Email", cell: (u) => u.email },
    { key: "phone", header: "Phone", cell: (u) => u.phoneNumber ?? "—" },
    { key: "id", header: "ID", cell: (u) => `#${u.id}`, className: "text-muted-foreground" },
    {
      key: "actions",
      header: "Actions",
      className: "text-right",
      cell: (u) => (
        <span className="flex justify-end gap-1">
          <Button type="button" variant="ghost" size="icon-sm" aria-label={`Edit ${fullName(u) || u.email}`} onClick={() => setEditing({ mode: "edit", user: u })}>
            <PencilIcon />
          </Button>
          <Button type="button" variant="ghost" size="icon-sm" aria-label={`Delete ${fullName(u) || u.email}`} onClick={() => setDeleting(u)}>
            <Trash2Icon />
          </Button>
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button type="button" onClick={() => setEditing({ mode: "add" })}>
          <PlusIcon /> Add user
        </Button>
      </div>
      <DataTable
        columns={columns}
        rows={users.data}
        getKey={(u) => u.id}
        isLoading={users.isPending}
        error={users.error}
        onRetry={() => void users.refetch()}
        emptyTitle="No users yet"
        emptyDescription="Add the first account with the button above."
      />
      <UserDialog
        open={editing.mode !== "closed"}
        onOpenChange={(open) => !open && setEditing({ mode: "closed" })}
        user={editing.mode === "edit" ? editing.user : undefined}
      />
      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Delete this user?"
        description={`${deleting ? fullName(deleting) || deleting.email : "This user"} will lose access, and their emergency contacts are removed too. This can't be undone.`}
        confirmLabel="Delete"
        isPending={remove.isPending}
        onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null), onError: () => setDeleting(null) })}
      />
    </div>
  );
}
