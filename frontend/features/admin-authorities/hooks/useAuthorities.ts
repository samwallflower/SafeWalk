"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { authoritiesApi } from "../api/authorities-api";
import type { AuthorityValues } from "../schemas/authority-schema";

const KEY = ["admin", "authorities"] as const;
const clean = (message: string) => message.replace(/^Error:\s*/, "");

export function useAuthorities() {
  return useQuery({ queryKey: KEY, queryFn: ({ signal }) => authoritiesApi.list(signal), staleTime: 60_000 });
}

export function useAddAuthority() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ["admin", "authorities", "add"],
    mutationFn: (values: AuthorityValues) => authoritiesApi.add(values),
    onSuccess: () => {
      toast.success("Authority added");
      return queryClient.invalidateQueries({ queryKey: KEY });
    },
  });
}

export function useUpdateAuthority(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ["admin", "authorities", "update", id],
    mutationFn: (values: AuthorityValues) => authoritiesApi.update(id, values),
    onSuccess: () => {
      toast.success("Authority updated");
      return queryClient.invalidateQueries({ queryKey: KEY });
    },
  });
}

export function useDeleteAuthority() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ["admin", "authorities", "delete"],
    mutationFn: (id: number) => authoritiesApi.remove(id),
    onSuccess: () => {
      toast.success("Authority deleted");
      return queryClient.invalidateQueries({ queryKey: KEY });
    },
    onError: (error) => toast.error(clean(error.message)),
  });
}
