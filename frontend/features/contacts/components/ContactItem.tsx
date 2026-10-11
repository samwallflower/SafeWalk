"use client";

import { MoreVerticalIcon } from "lucide-react";

import { InitialsAvatar } from "@/components/shared/InitialsAvatar";
import { Button } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";

import type { EmergencyContact } from "../types";

interface ContactItemProps {
  contact: EmergencyContact;
  onEdit?: (contact: EmergencyContact) => void;
  onDelete?: (contact: EmergencyContact) => void;
  /** Makes the row open the contact's details. */
  onOpen?: (contact: EmergencyContact) => void;
}

export function ContactItem({ contact, onEdit, onDelete, onOpen }: ContactItemProps) {
  return (
    <li className="flex items-center gap-3 rounded-xl border bg-card p-3">
      {onOpen ? (
        <button
          type="button"
          onClick={() => onOpen(contact)}
          aria-label={`Details for ${contact.contactName}`}
          className="flex min-w-0 flex-1 items-center gap-3 rounded-lg text-left outline-none hover:bg-accent focus-visible:ring-2 focus-visible:ring-ring"
        >
          <InitialsAvatar name={contact.contactName} className="bg-leaf text-foreground" />
          <span className="min-w-0 flex-1">
            <span className="block truncate font-semibold">{contact.contactName}</span>
            <span className="block truncate text-xs text-muted-foreground">
              {contact.contactEmail}
              {contact.contactPhone ? ` • ${contact.contactPhone}` : null}
            </span>
          </span>
        </button>
      ) : (
        <>
          <InitialsAvatar name={contact.contactName} className="bg-leaf text-foreground" />
          <div className="min-w-0 flex-1">
            <p className="truncate font-semibold">{contact.contactName}</p>
            <p className="truncate text-xs text-muted-foreground">
              {contact.contactEmail}
              {contact.contactPhone ? ` • ${contact.contactPhone}` : null}
            </p>
          </div>
        </>
      )}
      {onEdit && onDelete ? (
        <DropdownMenu>
          <DropdownMenuTrigger render={<Button variant="ghost" size="icon-sm" aria-label={`Actions for ${contact.contactName}`} />}>
            <MoreVerticalIcon />
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => onEdit(contact)}>Edit</DropdownMenuItem>
            <DropdownMenuItem onClick={() => onDelete(contact)}>Remove</DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      ) : null}
    </li>
  );
}
