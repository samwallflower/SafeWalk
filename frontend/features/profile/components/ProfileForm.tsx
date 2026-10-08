"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { FormError } from "@/components/shared/FormError";
import { FormField } from "@/components/shared/FormField";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import type { UserDto } from "@/features/auth/types";

import { useUpdateProfile } from "../hooks/useProfile";
import { profileSchema, type ProfileValues } from "../schemas/profile-schema";

export function ProfileForm({ profile }: { profile: UserDto }) {
  const update = useUpdateProfile();
  const {
    register,
    handleSubmit,
    formState: { errors, isDirty },
  } = useForm<ProfileValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: { firstName: profile.firstName, lastName: profile.lastName, phoneNumber: profile.phoneNumber ?? "" },
  });

  return (
    <form noValidate className="space-y-5" onSubmit={handleSubmit((values) => update.mutate(values))}>
      <FormError message={update.error ? update.error.message.replace(/^Error:\s*/, "") : null} />
      <FormField id="email" label="Email">
        <Input id="email" value={profile.email} readOnly disabled className="bg-muted" />
      </FormField>
      <div className="grid gap-4 sm:grid-cols-2">
        <FormField id="firstName" label="First name" error={errors.firstName?.message}>
          <Input id="firstName" autoComplete="given-name" aria-invalid={!!errors.firstName} {...register("firstName")} />
        </FormField>
        <FormField id="lastName" label="Last name" error={errors.lastName?.message}>
          <Input id="lastName" autoComplete="family-name" aria-invalid={!!errors.lastName} {...register("lastName")} />
        </FormField>
      </div>
      <FormField id="phoneNumber" label="Phone number" error={errors.phoneNumber?.message}>
        <Input id="phoneNumber" type="tel" autoComplete="tel" aria-invalid={!!errors.phoneNumber} {...register("phoneNumber")} />
      </FormField>
      <Button type="submit" disabled={update.isPending || !isDirty}>
        {update.isPending ? "Saving…" : "Save changes"}
      </Button>
    </form>
  );
}
