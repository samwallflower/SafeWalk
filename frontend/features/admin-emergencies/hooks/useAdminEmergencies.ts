"use client";

import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { TRIGGER_SOURCES } from "@/features/emergencies/lib/source-labels";

import { emergenciesAdminApi } from "../api/emergencies-admin-api";

const clean = (message: string) => message.replace(/^Error:\s*/, "");

export function useAllEmergencies() {
  return useQuery({
    queryKey: ["admin", "emergencies"],
    queryFn: ({ signal }) => emergenciesAdminApi.list(signal),
    staleTime: 30_000,
  });
}

/** One cheap count call per trigger source (no list download). */
export function useEmergencyCounts() {
  const results = useQueries({
    queries: TRIGGER_SOURCES.map((source) => ({
      queryKey: ["admin", "overview", "emergency-count", source],
      queryFn: ({ signal }: { signal: AbortSignal }) => emergenciesAdminApi.countBySource(source, signal),
      staleTime: 60_000,
    })),
  });
  return {
    isPending: results.some((r) => r.isPending),
    error: results.find((r) => r.isError)?.error ?? null,
    counts: TRIGGER_SOURCES.map((source, i) => ({ source, count: results[i].data ?? 0 })),
    refetch: () => results.forEach((r) => void r.refetch()),
  };
}

function useRefresh() {
  const queryClient = useQueryClient();
  return () =>
    Promise.all([
      queryClient.invalidateQueries({ queryKey: ["admin", "emergencies"] }),
      queryClient.invalidateQueries({ queryKey: ["admin", "overview"] }),
    ]);
}

export function useSetEmergencyResolved() {
  const refresh = useRefresh();
  return useMutation({
    mutationKey: ["admin", "emergencies", "resolve"],
    mutationFn: ({ id, resolved }: { id: number; resolved: boolean }) => emergenciesAdminApi.setResolved(id, resolved),
    onSuccess: (_e, { resolved }) => {
      toast.success(resolved ? "Marked as resolved" : "Marked as unresolved");
      return refresh();
    },
    onError: (error) => toast.error(clean(error.message)),
  });
}

export function useDeleteEmergency() {
  const refresh = useRefresh();
  return useMutation({
    mutationKey: ["admin", "emergencies", "delete"],
    mutationFn: (id: number) => emergenciesAdminApi.remove(id),
    onSuccess: () => {
      toast.success("Emergency deleted");
      return refresh();
    },
    onError: (error) => toast.error(clean(error.message)),
  });
}
