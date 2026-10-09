"use client";

import {
  AlertTriangleIcon,
  FootprintsIcon,
  SirenIcon,
  UsersIcon,
} from "lucide-react";
import { useMemo } from "react";

import { StatCard } from "@/components/shared/StatCard";
import { useAllEmergencies } from "@/features/admin-emergencies/hooks/useAdminEmergencies";
import { useAllSessions } from "@/features/admin-sessions/hooks/useAdminSessions";
import { useReportsByStatus } from "@/features/moderation/hooks/useModeration";
import { useTotalReports } from "@/features/reports-explorer/hooks/useTotalReports";
import { useAllUsers } from "@/features/users/hooks/useAllUsers";

import {
  countSessionsByStatus,
  emergencySummary,
  type StatsWindow,
} from "../lib/admin-stats";
import { useWindowReports } from "../hooks/useWindowReports";

const WINDOW_LABEL: Record<StatsWindow, string> = {
  "24h": "last 24 hours",
  "7d": "last 7 days",
  "30d": "last 30 days",
};

export function AdminStatsGrid({ window }: { window: StatsWindow }) {
  const users = useAllUsers();
  const reports = useWindowReports(window);
  const underReview = useReportsByStatus("UNDER_REVIEW");
  const total = useTotalReports();
  const hidden = useReportsByStatus("HIDDEN");
  const sessions = useAllSessions();
  const emergencies = useAllEmergencies();

  const sessionCounts = useMemo(
    () => (sessions.data ? countSessionsByStatus(sessions.data) : null),
    [sessions.data],
  );
  const emergencyCounts = useMemo(
    () => (emergencies.data ? emergencySummary(emergencies.data) : null),
    [emergencies.data],
  );
  const moderationLoaded = underReview.data && hidden.data;

  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-5">
      <StatCard
        label="Registered users"
        icon={<UsersIcon />}
        value={users.data ? String(users.data.length) : null}
        isLoading={users.isPending}
        isError={users.isError}
        detail="All accounts"
      />
      <StatCard
        label="Total reports"
        icon={<AlertTriangleIcon />}
        value={total.data !== undefined ? total.data.toLocaleString() : null}
        isLoading={total.isPending}
        isError={total.isError}
        detail="All statuses"
      />
      <StatCard
        label="New reports"
        icon={<AlertTriangleIcon />}
        value={reports.data ? reports.data.total.toLocaleString() : null}
        isLoading={reports.isPending}
        isError={reports.isError}
        detail={
          moderationLoaded
            ? `${WINDOW_LABEL[window]} • ${underReview.data.length} under review • ${hidden.data.length} hidden`
            : WINDOW_LABEL[window]
        }
      />
      <StatCard
        label="Walk sessions"
        icon={<FootprintsIcon />}
        value={sessions.data ? String(sessions.data.length) : null}
        isLoading={sessions.isPending}
        isError={sessions.isError}
        detail={
          sessionCounts
            ? `${sessionCounts.COMPLETED} completed • ${sessionCounts.ACTIVE} active`
            : null
        }
      />
      <StatCard
        label="Emergencies"
        icon={<SirenIcon />}
        value={emergencyCounts ? String(emergencyCounts.total) : null}
        isLoading={emergencies.isPending}
        isError={emergencies.isError}
        detail={
          emergencyCounts
            ? `${emergencyCounts.unresolved} unresolved • ${emergencyCounts.resolved} resolved`
            : null
        }
      />
    </div>
  );
}
