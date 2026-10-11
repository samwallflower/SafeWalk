"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { FormError } from "@/components/shared/FormError";
import { FormField } from "@/components/shared/FormField";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import type { UserDto } from "@/features/auth/types";

import { useCreateUser, useUpdateUser } from "../hooks/useUserMutations";
import { createUserSchema, editUserSchema, type CreateUserValues, type EditUserValues } from "../schemas/user-schemas";

interface UserDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Leave out to add a person; pass a user to edit them. */
  user?: UserDto;
}

export function UserDialog({ open, onOpenChange, user }: UserDialogProps) {
  const close = () => onOpenChange(false);
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        {open ? user ? <EditUserForm key={user.id} user={user} onDone={close} /> : <CreateUserForm onDone={close} /> : null}
      </DialogContent>
    </Dialog>
  );
}

function CreateUserForm({ onDone }: { onDone: () => void }) {
  const create = useCreateUser();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CreateUserValues>({
    resolver: zodResolver(createUserSchema),
    defaultValues: { firstName: "", lastName: "", email: "", password: "" },
  });
  return (
    <form noValidate className="space-y-4" onSubmit={handleSubmit((values) => create.mutate(values, { onSuccess: onDone }))}>
      <DialogHeader>
        <DialogTitle>Add user</DialogTitle>
        <DialogDescription>They get an email with a code to verify the account before signing in.</DialogDescription>
      </DialogHeader>
      <FormError message={create.error ? create.error.message.replace(/^Error:\s*/, "") : null} />
      <div className="grid gap-4 sm:grid-cols-2">
        <FormField id="new-first" label="First name" error={errors.firstName?.message}>
          <Input id="new-first" autoComplete="off" aria-invalid={!!errors.firstName} {...register("firstName")} />
        </FormField>
        <FormField id="new-last" label="Last name" error={errors.lastName?.message}>
          <Input id="new-last" autoComplete="off" aria-invalid={!!errors.lastName} {...register("lastName")} />
        </FormField>
      </div>
      <FormField id="new-email" label="Email" error={errors.email?.message}>
        <Input id="new-email" type="email" autoComplete="off" aria-invalid={!!errors.email} {...register("email")} />
      </FormField>
      <FormField id="new-password" label="Temporary password" error={errors.password?.message}>
        <Input id="new-password" type="password" autoComplete="new-password" aria-invalid={!!errors.password} {...register("password")} />
      </FormField>
      <DialogFooter>
        <Button type="button" variant="outline" onClick={onDone} disabled={create.isPending}>
          Cancel
        </Button>
        <Button type="submit" disabled={create.isPending}>
          {create.isPending ? "Adding…" : "Add user"}
        </Button>
      </DialogFooter>
    </form>
  );
}

function EditUserForm({ user, onDone }: { user: UserDto; onDone: () => void }) {
  const update = useUpdateUser(user.id);
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<EditUserValues>({
    resolver: zodResolver(editUserSchema),
    defaultValues: { firstName: user.firstName, lastName: user.lastName, phoneNumber: user.phoneNumber ?? "" },
  });
  return (
    <form noValidate className="space-y-4" onSubmit={handleSubmit((values) => update.mutate(values, { onSuccess: onDone }))}>
      <DialogHeader>
        <DialogTitle>Edit user</DialogTitle>
        <DialogDescription>{user.email}. The email address can&apos;t be changed.</DialogDescription>
      </DialogHeader>
      <FormError message={update.error ? update.error.message.replace(/^Error:\s*/, "") : null} />
      <div className="grid gap-4 sm:grid-cols-2">
        <FormField id="edit-first" label="First name" error={errors.firstName?.message}>
          <Input id="edit-first" aria-invalid={!!errors.firstName} {...register("firstName")} />
        </FormField>
        <FormField id="edit-last" label="Last name" error={errors.lastName?.message}>
          <Input id="edit-last" aria-invalid={!!errors.lastName} {...register("lastName")} />
        </FormField>
      </div>
      <FormField id="edit-phone" label="Phone" error={errors.phoneNumber?.message}>
        <Input id="edit-phone" type="tel" aria-invalid={!!errors.phoneNumber} {...register("phoneNumber")} />
      </FormField>
      <DialogFooter>
        <Button type="button" variant="outline" onClick={onDone} disabled={update.isPending}>
          Cancel
        </Button>
        <Button type="submit" disabled={update.isPending}>
          {update.isPending ? "Saving…" : "Save changes"}
        </Button>
      </DialogFooter>
    </form>
  );
}
