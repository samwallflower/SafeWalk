import type { Incident } from "@/features/incidents/types";
import type { WalkSession } from "@/features/sessions/types";

export interface SessionStats {
  total: number;
  completed: number;
  emergencies: number;
  completedPercent: number;
}

export interface ReportStats {
  total: number;
  active: number;
  hidden: number;
  underReview: number;
  upvotes: number;
  downvotes: number;
}

/** Pure aggregation of the user's own lists (no endpoint returns these totals). */
export function sessionStats(
  sessions: readonly Pick<WalkSession, "status">[],
): SessionStats {
  const completed = sessions.filter((s) => s.status === "COMPLETED").length;
  const emergencies = sessions.filter((s) => s.status === "EMERGENCY").length;
  return {
    total: sessions.length,
    completed,
    emergencies,
    completedPercent:
      sessions.length === 0
        ? 0
        : Math.round((completed / sessions.length) * 100),
  };
}

export function reportStats(
  reports: readonly Pick<Incident, "status" | "upvotes" | "downvotes">[],
): ReportStats {
  return {
    total: reports.length,
    active: reports.filter((r) => r.status === "ACTIVE").length,
    hidden: reports.filter((r) => r.status === "HIDDEN").length,
    underReview: reports.filter((r) => r.status === "UNDER_REVIEW").length,
    upvotes: reports.reduce((sum, r) => sum + r.upvotes, 0),
    downvotes: reports.reduce((sum, r) => sum + r.downvotes, 0),
  };
}
