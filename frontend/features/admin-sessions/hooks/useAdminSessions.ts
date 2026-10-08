"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { sessionsAdminApi } from "../api/sessions-admin-api";

export function useAllSessions() {
  return useQuery({
    queryKey: ["admin", "sessions"],
    queryFn: ({ signal }) => sessionsAdminApi.list(signal),
    staleTime: 30_000,
  });
}

export function useEndSession() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ["admin", "sessions", "end"],
    mutationFn: (id: number) => sessionsAdminApi.end(id),
    onSuccess: () => {
      toast.success("Session ended");
      return Promise.all([
        queryClient.invalidateQueries({ queryKey: ["admin", "sessions"] }),
        queryClient.invalidateQueries({ queryKey: ["admin", "overview"] }),
      ]);
    },
    onError: (error) => toast.error(error.message.replace(/^Error:\s*/, "")),
  });
}
