"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { categoriesAdminApi } from "../api/categories-admin-api";
import type { CategoryValues } from "../schemas/category-schema";

const clean = (message: string) => message.replace(/^Error:\s*/, "");

function useRefresh() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: ["categories"] });
}

export function useAddCategory() {
  const refresh = useRefresh();
  return useMutation({
    mutationKey: ["admin", "categories", "add"],
    mutationFn: (values: CategoryValues) => categoriesAdminApi.add(values),
    onSuccess: () => {
      toast.success("Category added");
      return refresh();
    },
  });
}

export function useUpdateCategory(id: number) {
  const refresh = useRefresh();
  return useMutation({
    mutationKey: ["admin", "categories", "update", id],
    mutationFn: (values: CategoryValues) => categoriesAdminApi.update(id, values),
    onSuccess: () => {
      toast.success("Category updated");
      return refresh();
    },
  });
}

export function useDeleteCategory() {
  const refresh = useRefresh();
  return useMutation({
    mutationKey: ["admin", "categories", "delete"],
    mutationFn: (id: number) => categoriesAdminApi.remove(id),
    onSuccess: () => {
      toast.success("Category deleted");
      return refresh();
    },
    onError: (error) => toast.error(clean(error.message)),
  });
}
