"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import type { ReportStatus } from "@/features/incidents/types";

import { moderationApi } from "../api/moderation-api";

export type ListedStatus = Exclude<ReportStatus, "ACTIVE">;

const clean = (message: string) => message.replace(/^Error:\s*/, "");

export function useReportsByStatus(status: ListedStatus) {
  return useQuery({
    queryKey: ["admin", "moderation", "status", status],
    queryFn: ({ signal }) => moderationApi.byStatus(status, signal),
    staleTime: 30_000,
  });
}

export function useReportLookup(id: number | null) {
  return useQuery({
    queryKey: ["admin", "moderation", "report", id],
    queryFn: ({ signal }) => moderationApi.byId(id as number, signal),
    enabled: id !== null,
    retry: false,
  });
}

function useRefresh() {
  const queryClient = useQueryClient();
  return () =>
    Promise.all([
      queryClient.invalidateQueries({ queryKey: ["admin", "moderation"] }),
      queryClient.invalidateQueries({ queryKey: ["admin", "overview"] }),
      queryClient.invalidateQueries({ queryKey: ["incidents"] }),
    ]);
}

export function useSetReportStatus() {
  const refresh = useRefresh();
  return useMutation({
    mutationKey: ["admin", "moderation", "set-status"],
    mutationFn: ({ id, status }: { id: number; status: ReportStatus }) =>
      moderationApi.setStatus(id, status),
    onSuccess: (_report, { status }) => {
      toast.success(
        status === "ACTIVE"
          ? "Report is visible on the map"
          : "Report status updated",
      );
      return refresh();
    },
    onError: (error) => toast.error(clean(error.message)),
  });
}

export function useDeleteAnyReport() {
  const refresh = useRefresh();
  return useMutation({
    mutationKey: ["admin", "moderation", "delete"],
    mutationFn: (id: number) => moderationApi.remove(id),
    onSuccess: () => {
      toast.success("Report deleted");
      return refresh();
    },
    onError: (error) => toast.error(clean(error.message)),
  });
}
