"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { useSession } from "@/features/auth/hooks/useSession";

import { contactsApi } from "../api/contacts-api";
import { toContactBody, type ContactValues } from "../schemas/contact-schema";
import { contactsKey } from "./useContacts";

const clean = (message: string) => message.replace(/^Error:\s*/, "");

function useInvalidateContacts() {
  const queryClient = useQueryClient();
  const { user } = useSession();
  return () => queryClient.invalidateQueries({ queryKey: contactsKey(user?.id) });
}

export function useAddContact() {
  const { user } = useSession();
  const invalidate = useInvalidateContacts();
  return useMutation({
    mutationKey: ["contacts", "add"],
    mutationFn: (values: ContactValues) => contactsApi.add(user!.id, toContactBody(values)),
    onSuccess: () => {
      toast.success("Contact added");
      return invalidate();
    },
  });
}

export function useUpdateContact(contactId: number) {
  const { user } = useSession();
  const invalidate = useInvalidateContacts();
  return useMutation({
    mutationKey: ["contacts", "update", contactId],
    mutationFn: (values: ContactValues) => contactsApi.update(user!.id, contactId, toContactBody(values)),
    onSuccess: () => {
      toast.success("Contact updated");
      return invalidate();
    },
  });
}

export function useDeleteContact() {
  const { user } = useSession();
  const invalidate = useInvalidateContacts();
  return useMutation({
    mutationKey: ["contacts", "delete"],
    mutationFn: (contactId: number) => contactsApi.remove(user!.id, contactId),
    onSuccess: () => {
      toast.success("Contact removed");
      return invalidate();
    },
    onError: (error) => toast.error(clean(error.message)),
  });
}
