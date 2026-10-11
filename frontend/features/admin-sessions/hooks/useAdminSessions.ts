"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import type { SessionStatus } from "@/features/sessions/types";

import { sessionsAdminApi } from "../api/sessions-admin-api";

const clean = (message: string) => message.replace(/^Error:\s*/, "");

export function useAllSessions() {
  return useQuery({
    queryKey: ["admin", "sessions"],
    queryFn: ({ signal }) => sessionsAdminApi.list(signal),
    staleTime: 30_000,
  });
}

function useRefresh() {
  const queryClient = useQueryClient();
  return () =>
    Promise.all([
      queryClient.invalidateQueries({ queryKey: ["admin", "sessions"] }),
      queryClient.invalidateQueries({ queryKey: ["admin", "overview"] }),
    ]);
}

export function useEndSession() {
  const refresh = useRefresh();
  return useMutation({
    mutationKey: ["admin", "sessions", "end"],
    mutationFn: (id: number) => sessionsAdminApi.end(id),
    onSuccess: () => {
      toast.success("Session ended");
      return refresh();
    },
    onError: (error) => toast.error(clean(error.message)),
  });
}

export function useSetSessionStatus() {
  const refresh = useRefresh();
  return useMutation({
    mutationKey: ["admin", "sessions", "status"],
    mutationFn: ({ id, status }: { id: number; status: Exclude<SessionStatus, "COMPLETED"> }) => sessionsAdminApi.setStatus(id, status),
    onSuccess: () => {
      toast.success("Status updated");
      return refresh();
    },
    onError: (error) => toast.error(clean(error.message)),
  });
}

export function useDeleteSession() {
  const refresh = useRefresh();
  return useMutation({
    mutationKey: ["admin", "sessions", "delete"],
    mutationFn: ({ id, userId }: { id: number; userId: number }) => sessionsAdminApi.remove(id, userId),
    onSuccess: () => {
      toast.success("Session deleted");
      return refresh();
    },
    // The server's reason is usually a database detail, so give a plain one.
    onError: () => toast.error("This session can't be deleted right now. It may still have emergencies on record."),
  });
}
