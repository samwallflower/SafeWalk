"use client";

import { UserPlusIcon } from "lucide-react";
import { useState } from "react";

import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { ListSkeleton } from "@/components/shared/ListSkeleton";
import { Button } from "@/components/ui/button";

import { useContacts } from "../hooks/useContacts";
import { useDeleteContact } from "../hooks/useContactMutations";
import type { EmergencyContact } from "../types";
import { ContactDialog } from "./ContactDialog";
import { ContactItem } from "./ContactItem";

type Editing = { mode: "closed" } | { mode: "add" } | { mode: "edit"; contact: EmergencyContact };

/** Full CRUD. The backend limits how many contacts a user may have and reports it when adding. */
export function ContactsManager() {
  const contacts = useContacts();
  const remove = useDeleteContact();
  const [editing, setEditing] = useState<Editing>({ mode: "closed" });
  const [deleting, setDeleting] = useState<EmergencyContact | null>(null);

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button type="button" onClick={() => setEditing({ mode: "add" })}>
          <UserPlusIcon /> Add trusted contact
        </Button>
      </div>

      {contacts.isPending ? (
        <ListSkeleton rows={3} />
      ) : contacts.isError ? (
        <ErrorState message={contacts.error.message} onRetry={() => void contacts.refetch()} />
      ) : contacts.data.length === 0 ? (
        <EmptyState
          title="No trusted contacts yet"
          description="Add people who should be notified if one of your walks becomes an emergency."
        />
      ) : (
        <ul className="space-y-3">
          {contacts.data.map((contact) => (
            <ContactItem key={contact.id} contact={contact} onEdit={(c) => setEditing({ mode: "edit", contact: c })} onDelete={setDeleting} />
          ))}
        </ul>
      )}

      <ContactDialog
        open={editing.mode !== "closed"}
        onOpenChange={(open) => !open && setEditing({ mode: "closed" })}
        contact={editing.mode === "edit" ? editing.contact : undefined}
      />
      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Remove this contact?"
        description={`${deleting?.contactName ?? "This contact"} will no longer be notified about your walks.`}
        confirmLabel="Remove"
        isPending={remove.isPending}
        onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })}
      />
    </div>
  );
}
