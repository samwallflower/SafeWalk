"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { KeyRoundIcon, MailIcon } from "lucide-react";
import { useRouter, useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";

import { FormError } from "@/components/shared/FormError";
import { FormField } from "@/components/shared/FormField";
import { IconInput } from "@/components/shared/IconInput";
import { Button } from "@/components/ui/button";

import { useResendVerification, useVerify } from "../hooks/useVerify";
import { verifySchema, type VerifyValues } from "../schemas/verify-schema";

export function VerifyCodeForm() {
  const router = useRouter();
  const params = useSearchParams();
  const verify = useVerify();
  const resend = useResendVerification();
  const {
    register,
    handleSubmit,
    getValues,
    trigger,
    formState: { errors },
  } = useForm<VerifyValues>({
    resolver: zodResolver(verifySchema),
    defaultValues: { email: params.get("email") ?? "", verificationCode: "" },
  });

  const onSubmit = handleSubmit((values) => verify.mutate(values, { onSuccess: () => router.push("/login") }));

  const onResend = async () => {
    if (await trigger("email")) resend.mutate(getValues("email"));
  };

  const error = verify.error?.message ?? resend.error?.message ?? null;

  return (
    <form onSubmit={onSubmit} className="space-y-5" noValidate>
      <FormError message={error} />
      {resend.isSuccess ? (
        <p role="status" className="text-sm font-medium text-success">
          A new code was sent to your email.
        </p>
      ) : null}
      <FormField id="email" label="Email address" error={errors.email?.message}>
        <IconInput id="email" type="email" autoComplete="email" icon={<MailIcon />} aria-invalid={!!errors.email} {...register("email")} />
      </FormField>
      <FormField id="verificationCode" label="Verification code" error={errors.verificationCode?.message}>
        <IconInput
          id="verificationCode"
          inputMode="numeric"
          autoComplete="one-time-code"
          icon={<KeyRoundIcon />}
          aria-invalid={!!errors.verificationCode}
          {...register("verificationCode")}
        />
      </FormField>
      <Button type="submit" size="lg" className="h-11 w-full text-base font-semibold" disabled={verify.isPending}>
        {verify.isPending ? "Verifying…" : "Verify email"}
      </Button>
      <Button type="button" variant="ghost" className="w-full" disabled={resend.isPending} onClick={onResend}>
        Resend code
      </Button>
    </form>
  );
}
