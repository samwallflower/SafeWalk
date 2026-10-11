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

const WEEKDAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"] as const;

/** Walks started on each of the last `days` days (today last), not counting abandoned ones. */
export function walksPerDay(
  sessions: readonly Pick<WalkSession, "status" | "startTime">[],
  days: number,
  now: Date = new Date(),
): { label: string; value: number }[] {
  const key = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
  const counts = new Map<string, number>();
  for (const s of sessions) {
    if (s.status === "ABANDONED") continue;
    // The backend writes local times without a zone, so the date part is the day it was started.
    const day = s.startTime.slice(0, 10);
    counts.set(day, (counts.get(day) ?? 0) + 1);
  }
  return Array.from({ length: days }, (_, i) => {
    const d = new Date(now.getFullYear(), now.getMonth(), now.getDate() - (days - 1 - i));
    return { label: WEEKDAYS[d.getDay()], value: counts.get(key(d)) ?? 0 };
  });
}

/** The user's reports counted per category name, largest first. */
export function reportsByCategory(reports: readonly Pick<Incident, "category">[]): { label: string; value: number }[] {
  const counts = new Map<string, number>();
  for (const r of reports) counts.set(r.category.name, (counts.get(r.category.name) ?? 0) + 1);
  return [...counts.entries()].map(([label, value]) => ({ label, value })).sort((a, b) => b.value - a.value);
}
