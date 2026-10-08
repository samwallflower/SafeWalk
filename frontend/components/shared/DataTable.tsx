import type { ReactNode } from "react";

import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { ListSkeleton } from "@/components/shared/ListSkeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { cn } from "@/lib/utils";

export interface Column<T> {
  key: string;
  header: string;
  cell: (row: T) => ReactNode;
  className?: string;
}

interface DataTableProps<T> {
  columns: readonly Column<T>[];
  rows: readonly T[] | undefined;
  getKey: (row: T) => string | number;
  isLoading: boolean;
  error: Error | null;
  onRetry: () => void;
  emptyTitle: string;
  emptyDescription?: string;
}

/** shadcn Table with the three data states built in: loading, error (with retry) and empty. */
export function DataTable<T>({ columns, rows, getKey, isLoading, error, onRetry, emptyTitle, emptyDescription }: DataTableProps<T>) {
  if (isLoading) return <ListSkeleton rows={5} />;
  if (error) return <ErrorState message={error.message} onRetry={onRetry} />;
  if (!rows || rows.length === 0) return <EmptyState title={emptyTitle} description={emptyDescription} />;
  return (
    <Table>
      <TableHeader>
        <TableRow>
          {columns.map((c) => (
            <TableHead key={c.key} className={cn("text-xs font-semibold tracking-wider uppercase", c.className)}>
              {c.header}
            </TableHead>
          ))}
        </TableRow>
      </TableHeader>
      <TableBody>
        {rows.map((row) => (
          <TableRow key={getKey(row)}>
            {columns.map((c) => (
              <TableCell key={c.key} className={c.className}>
                {c.cell(row)}
              </TableCell>
            ))}
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
