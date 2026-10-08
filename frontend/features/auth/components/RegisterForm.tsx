"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowRightIcon, MailIcon, UserIcon } from "lucide-react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";

import { FormError } from "@/components/shared/FormError";
import { FormField } from "@/components/shared/FormField";
import { IconInput } from "@/components/shared/IconInput";
import { Button } from "@/components/ui/button";

import { useRegister } from "../hooks/useRegister";
import { registerSchema, type RegisterValues } from "../schemas/register-schema";
import { PasswordInput } from "./PasswordInput";
import { PrivacyNote } from "./PrivacyNote";

export function RegisterForm() {
  const router = useRouter();
  const registerUser = useRegister();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { email: "", password: "", firstName: "", lastName: "" },
  });

  const onSubmit = handleSubmit((values) =>
    registerUser.mutate(values, {
      onSuccess: () => router.push(`/verify?email=${encodeURIComponent(values.email)}`),
    }),
  );

  return (
    <form onSubmit={onSubmit} className="space-y-5" noValidate>
      <FormError message={registerUser.error ? registerUser.error.message : null} />
      <div className="grid grid-cols-2 gap-3">
        <FormField id="firstName" label="First name" error={errors.firstName?.message}>
          <IconInput id="firstName" autoComplete="given-name" icon={<UserIcon />} aria-invalid={!!errors.firstName} {...register("firstName")} />
        </FormField>
        <FormField id="lastName" label="Last name" error={errors.lastName?.message}>
          <IconInput id="lastName" autoComplete="family-name" icon={<UserIcon />} aria-invalid={!!errors.lastName} {...register("lastName")} />
        </FormField>
      </div>
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
        <PasswordInput id="password" autoComplete="new-password" aria-invalid={!!errors.password} {...register("password")} />
      </FormField>
      <Button type="submit" size="lg" className="h-11 w-full text-base font-semibold" disabled={registerUser.isPending}>
        {registerUser.isPending ? "Creating account…" : <>Create Account <ArrowRightIcon /></>}
      </Button>
      <PrivacyNote />
    </form>
  );
}
