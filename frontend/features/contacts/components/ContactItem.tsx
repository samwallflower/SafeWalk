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
}

export function ContactItem({ contact, onEdit, onDelete }: ContactItemProps) {
  return (
    <li className="flex items-center gap-3 rounded-xl bg-muted p-3">
      <InitialsAvatar name={contact.contactName} className="bg-card" />
      <div className="min-w-0 flex-1">
        <p className="truncate font-semibold">{contact.contactName}</p>
        <p className="truncate text-xs text-muted-foreground">
          {contact.contactEmail}
          {contact.contactPhone ? ` • ${contact.contactPhone}` : null}
        </p>
      </div>
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
