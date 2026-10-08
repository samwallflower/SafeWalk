"use client";

import { MutationCache, QueryCache, QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useState, type ReactNode } from "react";

import { handleUnauthorized } from "@/features/auth/session-expiry";

function isAuthKey(key: readonly unknown[] | undefined): boolean {
  return key?.[0] === "auth";
}

function createClient(): QueryClient {
  return new QueryClient({
    queryCache: new QueryCache({
      onError: (error, query) => {
        if (!isAuthKey(query.queryKey)) handleUnauthorized(error);
      },
    }),
    mutationCache: new MutationCache({
      onError: (error, _vars, _ctx, mutation) => {
        if (!isAuthKey(mutation.options.mutationKey)) handleUnauthorized(error);
      },
    }),
    defaultOptions: { queries: { retry: false, refetchOnWindowFocus: false } },
  });
}

export function QueryProvider({ children }: { children: ReactNode }) {
  const [client] = useState(createClient);
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}
