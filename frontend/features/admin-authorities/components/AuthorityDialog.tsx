"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { FormError } from "@/components/shared/FormError";
import { FormField } from "@/components/shared/FormField";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";

import { useAddAuthority, useUpdateAuthority } from "../hooks/useAuthorities";
import { authoritySchema, type AuthorityValues } from "../schemas/authority-schema";
import type { EmergencyAuthority } from "../types";

interface AuthorityDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  authority?: EmergencyAuthority;
}

export function AuthorityDialog({ open, onOpenChange, authority }: AuthorityDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        {open ? <AuthorityForm key={authority?.id ?? "new"} authority={authority} onDone={() => onOpenChange(false)} /> : null}
      </DialogContent>
    </Dialog>
  );
}

function AuthorityForm({ authority, onDone }: { authority?: EmergencyAuthority; onDone: () => void }) {
  const add = useAddAuthority();
  const update = useUpdateAuthority(authority?.id ?? 0);
  const mutation = authority ? update : add;
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AuthorityValues>({
    resolver: zodResolver(authoritySchema),
    defaultValues: {
      countryCode: authority?.countryCode ?? "",
      countryName: authority?.countryName ?? "",
      policeNumber: authority?.policeNumber ?? "",
      ambulanceNumber: authority?.ambulanceNumber ?? "",
      generalEmergencyNumber: authority?.generalEmergencyNumber ?? "",
    },
  });

  return (
    <form noValidate className="space-y-4" onSubmit={handleSubmit((values) => mutation.mutate(values, { onSuccess: onDone }))}>
      <DialogHeader>
        <DialogTitle>{authority ? "Edit authority" : "Add authority"}</DialogTitle>
        <DialogDescription>Emergency numbers shown to walkers in this country.</DialogDescription>
      </DialogHeader>
      <FormError message={mutation.error ? mutation.error.message.replace(/^Error:\s*/, "") : null} />
      <div className="grid grid-cols-3 gap-3">
        <FormField id="auth-code" label="Code" error={errors.countryCode?.message}>
          <Input id="auth-code" maxLength={2} placeholder="HU" aria-invalid={!!errors.countryCode} {...register("countryCode", { setValueAs: (v: string) => v.toUpperCase() })} />
        </FormField>
        <div className="col-span-2">
          <FormField id="auth-name" label="Country" error={errors.countryName?.message}>
            <Input id="auth-name" aria-invalid={!!errors.countryName} {...register("countryName")} />
          </FormField>
        </div>
      </div>
      <div className="grid grid-cols-3 gap-3">
        <FormField id="auth-police" label="Police" error={errors.policeNumber?.message}>
          <Input id="auth-police" inputMode="numeric" aria-invalid={!!errors.policeNumber} {...register("policeNumber")} />
        </FormField>
        <FormField id="auth-ambulance" label="Ambulance" error={errors.ambulanceNumber?.message}>
          <Input id="auth-ambulance" inputMode="numeric" aria-invalid={!!errors.ambulanceNumber} {...register("ambulanceNumber")} />
        </FormField>
        <FormField id="auth-general" label="General" error={errors.generalEmergencyNumber?.message}>
          <Input id="auth-general" inputMode="numeric" aria-invalid={!!errors.generalEmergencyNumber} {...register("generalEmergencyNumber")} />
        </FormField>
      </div>
      <DialogFooter>
        <Button type="button" variant="outline" onClick={onDone} disabled={mutation.isPending}>
          Cancel
        </Button>
        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? "Saving…" : "Save authority"}
        </Button>
      </DialogFooter>
    </form>
  );
}
