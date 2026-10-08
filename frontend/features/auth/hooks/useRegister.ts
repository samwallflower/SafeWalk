"use client";

import { useMutation } from "@tanstack/react-query";

import { authApi } from "../api/auth-api";
import type { RegisterValues } from "../schemas/register-schema";

export function useRegister() {
  return useMutation({
    mutationKey: ["auth", "register"],
    mutationFn: (values: RegisterValues) => authApi.register(values),
  });
}
