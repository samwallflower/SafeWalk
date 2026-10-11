"use client";

import { MailIcon, PencilIcon, PhoneIcon, Trash2Icon } from "lucide-react";
import Link from "next/link";

import { InitialsAvatar } from "@/components/shared/InitialsAvatar";
import { Button, buttonVariants } from "@/components/ui/button";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { cn } from "@/lib/utils";

import type { EmergencyContact } from "../types";

interface ContactDrawerProps {
  contact: EmergencyContact | null;
  onOpenChange: (open: boolean) => void;
  /** With both handlers the drawer can change the contact; without them it links to the contacts page. */
  onEdit?: (contact: EmergencyContact) => void;
  onDelete?: (contact: EmergencyContact) => void;
}

/** Side drawer with everything about one emergency contact. */
export function ContactDrawer({ contact, onOpenChange, onEdit, onDelete }: ContactDrawerProps) {
  return (
    <Sheet open={contact !== null} onOpenChange={onOpenChange}>
      <SheetContent side="right" className="w-full gap-0 sm:max-w-md">
        {contact ? (
          <>
            <SheetHeader className="gap-4 p-6 pr-14">
              <InitialsAvatar name={contact.contactName} className="size-14 rounded-lg bg-leaf text-lg text-foreground" />
              <div>
                <SheetTitle className="text-2xl font-extrabold">{contact.contactName}</SheetTitle>
                <SheetDescription>Emergency contact</SheetDescription>
              </div>
            </SheetHeader>
            <div className="space-y-4 px-6">
              <dl className="divide-y rounded-xl border bg-card">
                <div className="flex items-start gap-3 p-4">
                  <PhoneIcon className="mt-0.5 size-5 text-primary" aria-hidden="true" />
                  <div className="min-w-0">
                    <dt className="text-xs font-semibold text-muted-foreground">Phone</dt>
                    <dd className="font-semibold break-words">{contact.contactPhone ?? "No phone number saved"}</dd>
                  </div>
                </div>
                <div className="flex items-start gap-3 p-4">
                  <MailIcon className="mt-0.5 size-5 text-primary" aria-hidden="true" />
                  <div className="min-w-0">
                    <dt className="text-xs font-semibold text-muted-foreground">Email</dt>
                    <dd className="font-semibold break-all">{contact.contactEmail}</dd>
                  </div>
                </div>
              </dl>
              <p className="text-sm text-muted-foreground">They get an email with your location if an emergency alert is raised.</p>
              {onEdit && onDelete ? (
                <div className="flex gap-3">
                  <Button type="button" variant="outline" className="h-11 flex-1 border-primary text-primary" onClick={() => onEdit(contact)}>
                    <PencilIcon /> Edit
                  </Button>
                  <Button type="button" variant="outline" className="h-11 flex-1 border-destructive text-destructive" onClick={() => onDelete(contact)}>
                    <Trash2Icon /> Remove
                  </Button>
                </div>
              ) : (
                <Link href="/dashboard/contacts" className={cn(buttonVariants({ variant: "outline" }), "h-11 w-full border-primary text-primary")}>
                  Manage contacts
                </Link>
              )}
            </div>
          </>
        ) : null}
      </SheetContent>
    </Sheet>
  );
}
