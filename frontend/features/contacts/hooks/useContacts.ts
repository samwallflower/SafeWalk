"use client";

import { useQuery } from "@tanstack/react-query";

import { useSession } from "@/features/auth/hooks/useSession";

import { contactsApi } from "../api/contacts-api";

export const contactsKey = (userId: number | undefined) => ["me", userId, "contacts"] as const;

export function useContacts() {
  const { user } = useSession();
  return useQuery({
    queryKey: contactsKey(user?.id),
    queryFn: ({ signal }) => contactsApi.list(user!.id, signal),
    enabled: user !== null,
    staleTime: 60_000,
  });
}
