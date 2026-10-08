"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";

import { FormError } from "@/components/shared/FormError";
import { FormField } from "@/components/shared/FormField";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { useCategories } from "@/features/categories/hooks/useCategories";
import type { Incident } from "@/features/incidents/types";

import { useUpdateReport } from "../hooks/useReportMutations";
import { editReportSchema, type EditReportValues } from "../schemas/edit-report-schema";

interface EditReportDialogProps {
  report: Incident | null;
  onOpenChange: (open: boolean) => void;
}

export function EditReportDialog({ report, onOpenChange }: EditReportDialogProps) {
  return (
    <Dialog open={report !== null} onOpenChange={onOpenChange}>
      <DialogContent>{report ? <EditForm key={report.id} report={report} onDone={() => onOpenChange(false)} /> : null}</DialogContent>
    </Dialog>
  );
}

function EditForm({ report, onDone }: { report: Incident; onDone: () => void }) {
  const categories = useCategories();
  const update = useUpdateReport(report.id, categories.data ?? []);
  const {
    control,
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<EditReportValues>({
    resolver: zodResolver(editReportSchema),
    defaultValues: { categoryId: report.category.id, description: report.description, isAnonymous: report.isAnonymous },
  });

  const items = (categories.data ?? []).map((c) => ({ value: String(c.id), label: c.name }));

  return (
    <form noValidate className="space-y-4" onSubmit={handleSubmit((values) => update.mutate(values, { onSuccess: onDone }))}>
      <DialogHeader>
        <DialogTitle>Edit report</DialogTitle>
        <DialogDescription>The location stays as originally reported.</DialogDescription>
      </DialogHeader>
      <FormError message={update.error ? update.error.message.replace(/^Error:\s*/, "") : null} />
      <FormField id="edit-category" label="Category" error={errors.categoryId?.message}>
        <Controller
          control={control}
          name="categoryId"
          render={({ field }) => (
            <Select items={items} value={String(field.value)} onValueChange={(v) => v !== null && field.onChange(Number(v))}>
              <SelectTrigger id="edit-category" className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {items.map((item) => (
                  <SelectItem key={item.value} value={item.value}>
                    {item.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        />
      </FormField>
      <FormField id="edit-description" label="Description" error={errors.description?.message}>
        <Textarea id="edit-description" rows={4} aria-invalid={!!errors.description} {...register("description")} />
      </FormField>
      <Controller
        control={control}
        name="isAnonymous"
        render={({ field }) => (
          <label className="flex items-center justify-between gap-3 text-sm font-medium">
            Post anonymously
            <Switch checked={field.value} onCheckedChange={field.onChange} />
          </label>
        )}
      />
      <DialogFooter>
        <Button type="button" variant="outline" onClick={onDone} disabled={update.isPending}>
          Cancel
        </Button>
        <Button type="submit" disabled={update.isPending || categories.isPending}>
          {update.isPending ? "Saving…" : "Save changes"}
        </Button>
      </DialogFooter>
    </form>
  );
}
