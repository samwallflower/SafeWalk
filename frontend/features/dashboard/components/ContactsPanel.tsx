"use client";

import { UsersIcon } from "lucide-react";

import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { SectionPanel } from "@/components/shared/SectionPanel";
import { ListSkeleton } from "@/components/shared/ListSkeleton";
import { ContactItem } from "@/features/contacts/components/ContactItem";
import { useContacts } from "@/features/contacts/hooks/useContacts";

export function ContactsPanel() {
  const contacts = useContacts();
  return (
    <SectionPanel
      title="Emergency contacts"
      icon={<UsersIcon />}
      action={{ href: "/dashboard/contacts", label: "Manage" }}
    >
      {contacts.isPending ? (
        <ListSkeleton rows={2} />
      ) : contacts.isError ? (
        <ErrorState
          message={contacts.error.message}
          onRetry={() => void contacts.refetch()}
        />
      ) : contacts.data.length === 0 ? (
        <EmptyState
          title="No trusted contacts yet"
          description="Add someone who should hear about an emergency."
        />
      ) : (
        <ul className="space-y-3">
          {contacts.data.slice(0, 3).map((c) => (
            <ContactItem key={c.id} contact={c} />
          ))}
        </ul>
      )}
    </SectionPanel>
  );
}
