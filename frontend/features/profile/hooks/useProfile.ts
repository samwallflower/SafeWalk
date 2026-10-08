"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { useSession } from "@/features/auth/hooks/useSession";

import { usersApi } from "../api/users-api";
import type { ProfileValues } from "../schemas/profile-schema";

export const profileKey = (userId: number | undefined) => ["me", userId, "profile"] as const;

export function useProfile() {
  const { user } = useSession();
  return useQuery({
    queryKey: profileKey(user?.id),
    queryFn: ({ signal }) => usersApi.get(user!.id, signal),
    enabled: user !== null,
    staleTime: 5 * 60_000,
  });
}

export function useUpdateProfile() {
  const { user } = useSession();
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ["profile", "update"],
    mutationFn: (values: ProfileValues) =>
      usersApi.update(user!.id, {
        firstName: values.firstName,
        lastName: values.lastName,
        ...(values.phoneNumber ? { phoneNumber: values.phoneNumber } : {}),
      }),
    onSuccess: (updated) => {
      queryClient.setQueryData(profileKey(user?.id), updated);
      toast.success("Profile saved");
    },
  });
}
