"use client";

import { PencilIcon, PlusIcon, Trash2Icon } from "lucide-react";
import { useState } from "react";

import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import { useCategories } from "@/features/categories/hooks/useCategories";
import type { IncidentCategory } from "@/features/categories/types";

import { useDeleteCategory } from "../hooks/useCategoryMutations";
import { CategoryDialog } from "./CategoryDialog";

type Editing = { mode: "closed" } | { mode: "add" } | { mode: "edit"; category: IncidentCategory };

export function CategoriesManager() {
  const categories = useCategories();
  const remove = useDeleteCategory();
  const [editing, setEditing] = useState<Editing>({ mode: "closed" });
  const [deleting, setDeleting] = useState<IncidentCategory | null>(null);

  const columns: readonly Column<IncidentCategory>[] = [
    { key: "name", header: "Category", cell: (c) => <span className="font-semibold">{c.name}</span> },
    { key: "severity", header: "Severity", cell: (c) => c.severityWeight },
    { key: "description", header: "Description", cell: (c) => <span className="text-muted-foreground">{c.description ?? "—"}</span> },
    {
      key: "actions",
      header: "Actions",
      className: "text-right",
      cell: (c) => (
        <span className="flex justify-end gap-1">
          <Button type="button" variant="ghost" size="icon-sm" aria-label={`Edit ${c.name}`} onClick={() => setEditing({ mode: "edit", category: c })}>
            <PencilIcon />
          </Button>
          <Button type="button" variant="ghost" size="icon-sm" aria-label={`Delete ${c.name}`} onClick={() => setDeleting(c)}>
            <Trash2Icon />
          </Button>
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button type="button" onClick={() => setEditing({ mode: "add" })}>
          <PlusIcon /> Add category
        </Button>
      </div>
      <DataTable
        columns={columns}
        rows={categories.data}
        getKey={(c) => c.id}
        isLoading={categories.isPending}
        error={categories.error}
        onRetry={() => void categories.refetch()}
        emptyTitle="No categories yet"
        emptyDescription="Reporters need at least one category to file a report."
      />
      <CategoryDialog
        open={editing.mode !== "closed"}
        onOpenChange={(open) => !open && setEditing({ mode: "closed" })}
        category={editing.mode === "edit" ? editing.category : undefined}
      />
      <ConfirmDialog
        open={deleting !== null}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Delete this category?"
        description={`${deleting?.name ?? "This category"} will be removed. The server may refuse if reports still use it.`}
        confirmLabel="Delete"
        isPending={remove.isPending}
        onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })}
      />
    </div>
  );
}
