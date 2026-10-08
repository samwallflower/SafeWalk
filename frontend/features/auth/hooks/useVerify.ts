"use client";

import { useMutation } from "@tanstack/react-query";

import { authApi } from "../api/auth-api";
import type { VerifyValues } from "../schemas/verify-schema";

export function useVerify() {
  return useMutation({
    mutationKey: ["auth", "verify"],
    mutationFn: (values: VerifyValues) => authApi.verify(values),
  });
}

export function useResendVerification() {
  return useMutation({
    mutationKey: ["auth", "resend"],
    mutationFn: (email: string) => authApi.resendVerification(email),
  });
}
