"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { FormError } from "@/components/shared/FormError";
import { FormField } from "@/components/shared/FormField";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import type { IncidentCategory } from "@/features/categories/types";

import { useAddCategory, useUpdateCategory } from "../hooks/useCategoryMutations";
import { categorySchema, type CategoryValues } from "../schemas/category-schema";

interface CategoryDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  category?: IncidentCategory;
}

export function CategoryDialog({ open, onOpenChange, category }: CategoryDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        {open ? <CategoryForm key={category?.id ?? "new"} category={category} onDone={() => onOpenChange(false)} /> : null}
      </DialogContent>
    </Dialog>
  );
}

function CategoryForm({ category, onDone }: { category?: IncidentCategory; onDone: () => void }) {
  const add = useAddCategory();
  const update = useUpdateCategory(category?.id ?? 0);
  const mutation = category ? update : add;
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CategoryValues>({
    resolver: zodResolver(categorySchema),
    defaultValues: { name: category?.name ?? "", severityWeight: category?.severityWeight ?? 5, description: category?.description ?? "" },
  });

  return (
    <form noValidate className="space-y-4" onSubmit={handleSubmit((values) => mutation.mutate(values, { onSuccess: onDone }))}>
      <DialogHeader>
        <DialogTitle>{category ? "Edit category" : "Add category"}</DialogTitle>
        <DialogDescription>Severity (1 to 20) decides how strongly reports in this category affect route safety.</DialogDescription>
      </DialogHeader>
      <FormError message={mutation.error ? mutation.error.message.replace(/^Error:\s*/, "") : null} />
      <FormField id="cat-name" label="Name" error={errors.name?.message}>
        <Input id="cat-name" aria-invalid={!!errors.name} {...register("name")} />
      </FormField>
      <FormField id="cat-severity" label="Severity weight" error={errors.severityWeight?.message}>
        <Input id="cat-severity" type="number" min={1} max={20} aria-invalid={!!errors.severityWeight} {...register("severityWeight", { valueAsNumber: true })} />
      </FormField>
      <FormField id="cat-description" label="Description" error={errors.description?.message}>
        <Textarea id="cat-description" rows={3} {...register("description")} />
      </FormField>
      <DialogFooter>
        <Button type="button" variant="outline" onClick={onDone} disabled={mutation.isPending}>
          Cancel
        </Button>
        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? "Saving…" : "Save category"}
        </Button>
      </DialogFooter>
    </form>
  );
}
