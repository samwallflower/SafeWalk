"use client";

import { preconnect } from "react-dom";

import { useMemo, useState } from "react";

import { EmptyState } from "@/components/shared/EmptyState";
import { ErrorState } from "@/components/shared/ErrorState";
import { PageHeader } from "@/components/shared/PageHeader";
import { Skeleton } from "@/components/ui/skeleton";
import { useSession } from "@/features/auth/hooks/useSession";
import { useCategories } from "@/features/categories/hooks/useCategories";
import type { ReportStatus } from "@/features/incidents/types";
import { DEFAULT_VIEW } from "@/features/map/config";
import { useMapUi } from "@/features/map/store/mapFilters";
import type { Place } from "@/features/places/types";

import { useAreaReports } from "../hooks/useAreaReports";
import {
  DEFAULT_FILTERS,
  filterReports,
  filtersAreDefault,
  paginate,
  sortReports,
  type ExplorerFilterValues,
} from "../lib/explorer-filters";
import { AllReportsPanel } from "./AllReportsPanel";
import { AreaPicker } from "./AreaPicker";
import { ExplorerFilters } from "./ExplorerFilters";
import { ReportsMapLazy } from "./ReportsMapLazy";
import { ReportsTable } from "./ReportsTable";
import { ScopeToggle, type ExplorerScope } from "./ScopeToggle";
import { TablePagination } from "./TablePagination";
import { ViewToggle, type ExplorerView } from "./ViewToggle";

const PAGE_SIZE = 25;
const DEFAULT_RADIUS_M = 500;
/** Above this many results in one area the hint suggests narrowing it (payload size). */
const LARGE_RESULT_SET = 3000;

export function ExplorerScreen() {
  // Start the Mapbox connection while the map code is still loading.
  preconnect("https://api.mapbox.com");
  const lastCenter = useMapUi((s) => s.lastCenter);
  const { isAdmin } = useSession();
  const categories = useCategories();

  const [center, setCenter] = useState<Place>(() => ({
    label: "Current map area",
    latitude: (lastCenter ?? DEFAULT_VIEW).latitude,
    longitude: (lastCenter ?? DEFAULT_VIEW).longitude,
  }));
  const [radiusMeters, setRadiusMeters] = useState(DEFAULT_RADIUS_M);
  const [status, setStatus] = useState<ReportStatus>("ACTIVE");
  const [filters, setFilters] = useState<ExplorerFilterValues>(DEFAULT_FILTERS);
  const [scope, setScope] = useState<ExplorerScope>("area");
  const [view, setView] = useState<ExplorerView>("table");
  const [page, setPage] = useState(1);

  const reports = useAreaReports({
    latitude: center.latitude,
    longitude: center.longitude,
    radiusMeters,
    status: isAdmin ? status : "ACTIVE",
  });

  const filtered = useMemo(
    () =>
      reports.data
        ? sortReports(filterReports(reports.data, filters), filters.sort)
        : undefined,
    [reports.data, filters],
  );
  const paged = useMemo(
    () => paginate(filtered ?? [], page, PAGE_SIZE),
    [filtered, page],
  );

  const changeFilters = (next: ExplorerFilterValues) => {
    setFilters(next);
    setPage(1);
  };

  return (
    <div className="mx-auto w-full max-w-7xl space-y-5 px-4 py-8 md:px-6">
      <PageHeader
        title="Incident reports"
        description="Browse reports around a place as a table or on the map, or page through every active report."
        actions={
          <div className="flex flex-wrap items-center gap-2">
            <ScopeToggle value={scope} onChange={setScope} />
            {scope === "area" ? (
              <ViewToggle value={view} onChange={setView} />
            ) : null}
          </div>
        }
      />

      {scope === "all" ? <AllReportsPanel /> : null}

      {scope === "area" ? (
        <>
          <section className="space-y-3 rounded-2xl bg-card p-4 shadow-sm ring-1 ring-foreground/5">
            <AreaPicker
              center={center}
              radiusMeters={radiusMeters}
              onCenter={(place) => {
                setCenter(place);
                setPage(1);
              }}
              onRadius={(meters) => {
                setRadiusMeters(meters);
                setPage(1);
              }}
            />
            {categories.isError ? (
              <p className="text-xs text-destructive">
                Categories failed to load; the category filter is unavailable.
              </p>
            ) : null}
            <ExplorerFilters
              categories={categories.data ?? []}
              filters={filters}
              onChange={changeFilters}
              canChooseStatus={isAdmin}
              status={status}
              onStatus={(s) => {
                setStatus(s);
                setPage(1);
              }}
            />
          </section>

          <p className="text-sm text-muted-foreground" aria-live="polite">
            {reports.isPending
              ? "Loading reports…"
              : reports.isError
                ? null
                : `${(filtered?.length ?? 0).toLocaleString()} of ${(reports.data?.length ?? 0).toLocaleString()} reports within ${radiusMeters >= 1000 ? `${radiusMeters / 1000} km` : `${radiusMeters} m`} of ${center.label}`}
          </p>

          {(reports.data?.length ?? 0) > LARGE_RESULT_SET ? (
            <p className="text-xs text-muted-foreground">
              This area has a lot of reports. A smaller radius loads faster.
            </p>
          ) : null}

          {view === "table" ? (
            <section className="space-y-2 rounded-2xl bg-card p-4 shadow-sm ring-1 ring-foreground/5">
              <ReportsTable
                rows={paged.items}
                isLoading={reports.isPending}
                error={reports.error}
                onRetry={() => void reports.refetch()}
                filtered={!filtersAreDefault(filters)}
              />
              <TablePagination
                page={paged.page}
                pageCount={paged.pageCount}
                total={paged.total}
                pageSize={PAGE_SIZE}
                onPage={setPage}
              />
            </section>
          ) : (
            <section className="h-[60dvh] min-h-96 overflow-hidden rounded-2xl bg-card shadow-sm ring-1 ring-foreground/5">
              {reports.isPending ? (
                <Skeleton
                  className="h-full w-full rounded-none"
                  role="status"
                  aria-label="Loading map"
                />
              ) : reports.isError ? (
                <div className="p-4">
                  <ErrorState
                    message={reports.error.message}
                    onRetry={() => void reports.refetch()}
                  />
                </div>
              ) : (filtered?.length ?? 0) === 0 ? (
                <div className="p-4">
                  <EmptyState
                    title="No reports to show"
                    description="Widen the radius or clear the filters."
                  />
                </div>
              ) : (
                <ReportsMapLazy
                  reports={filtered ?? []}
                  latitude={center.latitude}
                  longitude={center.longitude}
                  radiusMeters={radiusMeters}
                />
              )}
            </section>
          )}
        </>
      ) : null}
    </div>
  );
}
