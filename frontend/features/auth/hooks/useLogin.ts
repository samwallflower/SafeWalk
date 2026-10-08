"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";

import { authApi } from "../api/auth-api";
import type { LoginValues } from "../schemas/login-schema";
import { SESSION_QUERY_KEY } from "./useSession";

export function useLogin() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ["auth", "login"],
    mutationFn: (values: LoginValues) => authApi.login(values),
    onSuccess: (user) => queryClient.setQueryData(SESSION_QUERY_KEY, user),
  });
}
