"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import type { IncidentCategory } from "@/features/categories/types";
import type { EditReportValues } from "@/features/my-reports/schemas/edit-report-schema";

import { reportsAdminApi, type AdminReport } from "../api/reports-admin-api";

export function useAdminUpdateReport(report: AdminReport, categories: readonly IncidentCategory[]) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ["admin", "reports", "update", report.id],
    mutationFn: (values: EditReportValues) => {
      const category = categories.find((c) => c.id === values.categoryId);
      if (!category) throw new Error("Pick a category");
      if (report.reporterId === null) throw new Error("This report has no reporter on record, so it can't be edited.");
      return reportsAdminApi.update(report.reporterId, report.id, {
        description: values.description,
        isAnonymous: values.isAnonymous,
        category: { id: category.id, name: category.name },
      });
    },
    onSuccess: () => {
      toast.success("Report updated");
      return Promise.all([
        queryClient.invalidateQueries({ queryKey: ["admin", "moderation"] }),
        queryClient.invalidateQueries({ queryKey: ["incidents"] }),
      ]);
    },
  });
}
