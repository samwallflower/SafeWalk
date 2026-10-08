"use client";

import { PencilIcon, PlusIcon, Trash2Icon } from "lucide-react";
import { useState } from "react";

import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";

import { useAuthorities, useDeleteAuthority } from "../hooks/useAuthorities";
import type { EmergencyAuthority } from "../types";
import { AuthorityDialog } from "./AuthorityDialog";

type Editing = { mode: "closed" } | { mode: "add" } | { mode: "edit"; authority: EmergencyAuthority };

export function AuthoritiesManager() {
  const authorities = useAuthorities();
  const remove = useDeleteAuthority();
  const [editing, setEditing] = useState<Editing>({ mode: "closed" });
  const [deleting, setDeleting] = useState<EmergencyAuthority | null>(null);

  const columns: readonly Column<EmergencyAuthority>[] = [
    { key: "country", header: "Country", cell: (a) => <span className="font-semibold">{a.countryName}</span> },
    { key: "code", header: "Code", cell: (a) => a.countryCode },
    { key: "police", header: "Police", cell: (a) => a.policeNumber },
    { key: "ambulance", header: "Ambulance", cell: (a) => a.ambulanceNumber },
    { key: "general", header: "General", cell: (a) => a.generalEmergencyNumber ?? "—" },
    {
      key: "actions",
      header: "Actions",
      className: "text-right",
      cell: (a) => (
        <span className="flex justify-end gap-1">
          <Button type="button" variant="ghost" size="icon-sm" aria-label={`Edit ${a.countryName}`} onClick={() => setEditing({ mode: "edit", authority: a })}>
            <PencilIcon />
          </Button>
          <Button type="button" variant="ghost" size="icon-sm" aria-label={`Delete ${a.countryName}`} onClick={() => setDeleting(a)}>
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
          <PlusIcon /> Add authority
        </Button>
      </div>
      <DataTable
        columns={columns}
        rows={authorities.data}
        getKey={(a) => a.id}
        isLoading={authorities.isPending}
        error={authorities.error}
        onRetry={() => void authorities.refetch()}
        emptyTitle="No authorities yet"
        emptyDescription="Mobile users get their local emergency numbers from these entries."
      />
      <AuthorityDialog
        open={editing.mode !== "closed"}
        onOpenChange={(open) => !open && setEditing({ mode: "closed" })}
        authority={editing.mode === "edit" ? editing.authority : undefined}
      />
      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Delete this authority?"
        description={`Walkers in ${deleting?.countryName ?? "this country"} will no longer get these numbers.`}
        confirmLabel="Delete"
        isPending={remove.isPending}
        onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })}
      />
    </div>
  );
}
