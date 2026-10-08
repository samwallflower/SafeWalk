"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { SendIcon } from "lucide-react";
import Link from "next/link";
import { useState } from "react";
import { Controller, useForm, useWatch } from "react-hook-form";

import { FormError } from "@/components/shared/FormError";
import { Button, buttonVariants } from "@/components/ui/button";
import { useCategories } from "@/features/categories/hooks/useCategories";
import { DEFAULT_VIEW } from "@/features/map/config";
import { useMapUi } from "@/features/map/store/mapFilters";
import { ApiError } from "@/lib/http/api-error";

import { useCreateReport } from "../hooks/useCreateReport";
import { reportSchema, type ReportValues } from "../schemas/report-schema";
import { AnonymousSection } from "./AnonymousSection";
import { CategorySection } from "./CategorySection";
import { DescriptionSection } from "./DescriptionSection";
import { GuardNote } from "./GuardNote";
import { LocationSection } from "./LocationSection";
import { cn } from "@/lib/utils";

function submitErrorMessage(error: Error): string {
  if (error instanceof ApiError && error.status === 429) {
    return `${error.message.replace(/^Error:\s*/, "")}`;
  }
  return error.message.replace(/^Error:\s*/, "");
}

export function ReportForm() {
  const categories = useCategories();
  const create = useCreateReport(categories.data ?? []);
  const lastCenter = useMapUi((s) => s.lastCenter);
  const start = lastCenter ?? DEFAULT_VIEW;
  const [fromGps, setFromGps] = useState(false);

  const {
    control,
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<ReportValues>({
    resolver: zodResolver(reportSchema),
    defaultValues: {
      description: "",
      isAnonymous: true,
      latitude: start.latitude,
      longitude: start.longitude,
    },
  });

  const [latitude, longitude, description] = useWatch({ control, name: ["latitude", "longitude", "description"] });

  return (
    <form onSubmit={handleSubmit((values) => create.mutate(values))} className="space-y-5" noValidate>
      <LocationSection
        value={{ latitude, longitude }}
        fromGps={fromGps}
        onChange={(position, gps) => {
          setValue("latitude", position.latitude, { shouldValidate: true });
          setValue("longitude", position.longitude, { shouldValidate: true });
          setFromGps(gps);
        }}
        error={errors.latitude?.message ?? errors.longitude?.message}
      />

      <Controller
        control={control}
        name="categoryId"
        render={({ field }) => (
          <CategorySection
            categories={categories.data}
            isLoading={categories.isPending}
            error={categories.error}
            onRetry={() => void categories.refetch()}
            value={field.value}
            onChange={field.onChange}
            fieldError={errors.categoryId?.message}
          />
        )}
      />

      <DescriptionSection length={description.length} fieldError={errors.description?.message} {...register("description")} />

      <Controller
        control={control}
        name="isAnonymous"
        render={({ field }) => <AnonymousSection checked={field.value} onChange={field.onChange} />}
      />

      <GuardNote />

      <FormError message={create.error ? submitErrorMessage(create.error) : null} />

      <div className="flex flex-wrap justify-end gap-3">
        <Link href="/map" className={cn(buttonVariants({ variant: "secondary", size: "lg" }), "h-11 px-6")}>
          Cancel
        </Link>
        <Button type="submit" size="lg" className="h-11 px-6 font-semibold" disabled={create.isPending}>
          <SendIcon /> {create.isPending ? "Publishing…" : "Publish Incident to Map"}
        </Button>
      </div>
    </form>
  );
}
