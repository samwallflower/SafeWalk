"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { usersAdminApi } from "../api/users-admin-api";
import type { CreateUserValues, EditUserValues } from "../schemas/user-schemas";

const clean = (message: string) => message.replace(/^Error:\s*/, "");

function useRefresh() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
}

export function useCreateUser() {
  const refresh = useRefresh();
  return useMutation({
    mutationKey: ["admin", "users", "create"],
    mutationFn: (values: CreateUserValues) => usersAdminApi.create(values),
    onSuccess: () => {
      toast.success("User added. They were emailed a code to verify their account.");
      return refresh();
    },
  });
}

export function useUpdateUser(id: number) {
  const refresh = useRefresh();
  return useMutation({
    mutationKey: ["admin", "users", "update", id],
    mutationFn: (values: EditUserValues) => usersAdminApi.update(id, values),
    onSuccess: () => {
      toast.success("User updated");
      return refresh();
    },
  });
}

export function useDeleteUser() {
  const refresh = useRefresh();
  return useMutation({
    mutationKey: ["admin", "users", "delete"],
    mutationFn: (id: number) => usersAdminApi.remove(id),
    onSuccess: () => {
      toast.success("User deleted");
      return refresh();
    },
    // The server's reason is usually a database detail, so give a plain one.
    onError: (error) =>
      toast.error(
        error.message.includes("not found") ? clean(error.message) : "This user can't be deleted right now. They may still have walks or reports on record.",
      ),
  });
}
