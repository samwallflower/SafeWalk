"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowRightIcon, MailIcon } from "lucide-react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";

import { FormError } from "@/components/shared/FormError";
import { FormField } from "@/components/shared/FormField";
import { IconInput } from "@/components/shared/IconInput";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/http/api-error";

import { useLogin } from "../hooks/useLogin";
import { homeFor } from "../route-guard";
import { safeRedirect } from "../safe-redirect";
import { loginSchema, type LoginValues } from "../schemas/login-schema";
import { DemoLoginButtons } from "./DemoLoginButtons";
import { PasswordInput } from "./PasswordInput";
import { PrivacyNote } from "./PrivacyNote";

export function LoginForm() {
  const router = useRouter();
  const params = useSearchParams();
  const login = useLogin();
  const {
    register,
    handleSubmit,
    setValue,
    setFocus,
    getValues,
    formState: { errors },
  } = useForm<LoginValues>({ resolver: zodResolver(loginSchema), defaultValues: { email: "", password: "" } });

  const onSubmit = handleSubmit((values) =>
    login.mutate(values, {
      onSuccess: (user) => {
        router.replace(safeRedirect(params.get("redirect")) ?? homeFor(user));
        router.refresh();
      },
    }),
  );

  const unverified = login.error instanceof ApiError && login.error.status === 403;
  const expiredNotice = params.get("expired") ? "Your session expired. Please log in again." : null;
  const errorMessage = login.error ? login.error.message : expiredNotice;

  return (
    <form onSubmit={onSubmit} className="space-y-5" noValidate>
      <FormError message={errorMessage} />
      {unverified ? (
        <p className="text-sm">
          <Link className="font-medium text-primary underline" href={`/verify?email=${encodeURIComponent(getValues("email"))}`}>
            Verify your email
          </Link>
        </p>
      ) : null}
      <FormField id="email" label="Email address" error={errors.email?.message}>
        <IconInput
          id="email"
          type="email"
          autoComplete="email"
          placeholder="you@domain.com"
          icon={<MailIcon />}
          aria-invalid={!!errors.email}
          {...register("email")}
        />
      </FormField>
      <FormField id="password" label="Password" error={errors.password?.message}>
        <PasswordInput id="password" autoComplete="current-password" aria-invalid={!!errors.password} {...register("password")} />
      </FormField>
      <Button type="submit" size="lg" className="h-11 w-full text-base font-semibold" disabled={login.isPending}>
        {login.isPending ? "Signing in…" : <>Sign In <ArrowRightIcon /></>}
      </Button>
      <DemoLoginButtons
        onFill={(values) => {
          setValue("email", values.email, { shouldValidate: true });
          setValue("password", values.password, { shouldValidate: true });
          setFocus("password");
        }}
      />
      <PrivacyNote />
    </form>
  );
}
