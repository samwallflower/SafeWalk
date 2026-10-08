"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

import type { IncidentCategory } from "@/features/categories/types";
import { useSession } from "@/features/auth/hooks/useSession";
import { incidentsApi } from "@/features/incidents/api/incidents-api";

import type { ReportValues } from "../schemas/report-schema";

export function useCreateReport(categories: readonly IncidentCategory[]) {
  const queryClient = useQueryClient();
  const router = useRouter();
  const { user } = useSession();

  return useMutation({
    mutationKey: ["report", "create"],
    mutationFn: (values: ReportValues) => {
      const category = categories.find((c) => c.id === values.categoryId);
      if (!user || !category) throw new Error("Please sign in and pick a category");
      return incidentsApi.create(user.id, {
        description: values.description,
        latitude: values.latitude,
        longitude: values.longitude,
        isAnonymous: values.isAnonymous,
        category: { id: category.id, name: category.name },
      });
    },
    onSuccess: (incident) => {
      void queryClient.invalidateQueries({ queryKey: ["incidents"] });
      toast.success("Incident published to the map");
      router.push(`/incidents/${incident.id}`);
    },
  });
}
