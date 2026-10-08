"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { FormError } from "@/components/shared/FormError";
import { FormField } from "@/components/shared/FormField";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";

import { useAddContact, useUpdateContact } from "../hooks/useContactMutations";
import { contactSchema, type ContactValues } from "../schemas/contact-schema";
import type { EmergencyContact } from "../types";

interface ContactDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Present when editing, absent when adding. */
  contact?: EmergencyContact;
}

export function ContactDialog({ open, onOpenChange, contact }: ContactDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        {/* Remounted per open so the form always starts from the right values. */}
        {open ? <ContactForm key={contact?.id ?? "new"} contact={contact} onDone={() => onOpenChange(false)} /> : null}
      </DialogContent>
    </Dialog>
  );
}

function ContactForm({ contact, onDone }: { contact?: EmergencyContact; onDone: () => void }) {
  const add = useAddContact();
  const update = useUpdateContact(contact?.id ?? 0);
  const mutation = contact ? update : add;

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ContactValues>({
    resolver: zodResolver(contactSchema),
    defaultValues: {
      contactName: contact?.contactName ?? "",
      contactEmail: contact?.contactEmail ?? "",
      contactPhone: contact?.contactPhone ?? "",
    },
  });

  return (
    <form
      noValidate
      className="space-y-4"
      onSubmit={handleSubmit((values) => mutation.mutate(values, { onSuccess: onDone }))}
    >
      <DialogHeader>
        <DialogTitle>{contact ? "Edit contact" : "Add trusted contact"}</DialogTitle>
        <DialogDescription>They are notified if one of your walks becomes an emergency.</DialogDescription>
      </DialogHeader>
      <FormError message={mutation.error ? mutation.error.message.replace(/^Error:\s*/, "") : null} />
      <FormField id="contactName" label="Name" error={errors.contactName?.message}>
        <Input id="contactName" autoComplete="off" aria-invalid={!!errors.contactName} {...register("contactName")} />
      </FormField>
      <FormField id="contactEmail" label="Email" error={errors.contactEmail?.message}>
        <Input id="contactEmail" type="email" autoComplete="off" aria-invalid={!!errors.contactEmail} {...register("contactEmail")} />
      </FormField>
      <FormField id="contactPhone" label="Phone (optional)" error={errors.contactPhone?.message}>
        <Input id="contactPhone" placeholder="+36301234567" autoComplete="off" aria-invalid={!!errors.contactPhone} {...register("contactPhone")} />
      </FormField>
      <DialogFooter>
        <Button type="button" variant="outline" onClick={onDone} disabled={mutation.isPending}>
          Cancel
        </Button>
        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? "Saving…" : "Save contact"}
        </Button>
      </DialogFooter>
    </form>
  );
}
