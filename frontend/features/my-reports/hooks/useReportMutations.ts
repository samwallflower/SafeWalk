"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { useSession } from "@/features/auth/hooks/useSession";
import type { IncidentCategory } from "@/features/categories/types";

import { myReportsApi } from "../api/my-reports-api";
import type { EditReportValues } from "../schemas/edit-report-schema";
import { myReportsKey } from "./useMyReports";

const clean = (message: string) => message.replace(/^Error:\s*/, "");

function useRefreshReports() {
  const queryClient = useQueryClient();
  const { user } = useSession();
  return () =>
    Promise.all([
      queryClient.invalidateQueries({ queryKey: myReportsKey(user?.id) }),
      queryClient.invalidateQueries({ queryKey: ["incidents"] }),
    ]);
}

export function useUpdateReport(
  reportId: number,
  categories: readonly IncidentCategory[],
) {
  const { user } = useSession();
  const refresh = useRefreshReports();
  return useMutation({
    mutationKey: ["my-reports", "update", reportId],
    mutationFn: (values: EditReportValues) => {
      const category = categories.find((c) => c.id === values.categoryId);
      if (!category) throw new Error("Pick a category");
      return myReportsApi.update(user!.id, reportId, {
        description: values.description,
        isAnonymous: values.isAnonymous,
        category: { id: category.id, name: category.name },
      });
    },
    onSuccess: () => {
      toast.success("Report updated");
      return refresh();
    },
  });
}

export function useDeleteReport() {
  const { user } = useSession();
  const refresh = useRefreshReports();
  return useMutation({
    mutationKey: ["my-reports", "delete"],
    mutationFn: (reportId: number) => myReportsApi.remove(user!.id, reportId),
    onSuccess: () => {
      toast.success("Report deleted");
      return refresh();
    },
    onError: (error) => toast.error(clean(error.message)),
  });
}
