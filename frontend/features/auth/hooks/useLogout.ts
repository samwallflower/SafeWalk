"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";

import { authApi } from "../api/auth-api";
import { SESSION_QUERY_KEY } from "./useSession";

export function useLogout() {
  const queryClient = useQueryClient();
  const router = useRouter();
  return useMutation({
    mutationKey: ["auth", "logout"],
    mutationFn: authApi.logout,
    onSuccess: () => {
      queryClient.clear();
      queryClient.setQueryData(SESSION_QUERY_KEY, null);
      router.push("/");
      router.refresh();
    },
  });
}
