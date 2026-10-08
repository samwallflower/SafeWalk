import type { Incident } from "@/features/incidents/types";
import type { Emergency } from "@/features/emergencies/types";
import type { WalkSession } from "@/features/sessions/types";

export type StatsWindow = "24h" | "7d" | "30d";

export const WINDOW_DAYS: Record<StatsWindow, number> = {
  "24h": 1,
  "7d": 7,
  "30d": 30,
};

export interface CountItem {
  label: string;
  value: number;
}

/** Counts reports per category name, largest first. */
export function countByCategory(
  reports: readonly Pick<Incident, "category">[],
): CountItem[] {
  const counts = new Map<string, number>();
  for (const r of reports)
    counts.set(r.category.name, (counts.get(r.category.name) ?? 0) + 1);
  return [...counts.entries()]
    .map(([label, value]) => ({ label, value }))
    .sort((a, b) => b.value - a.value);
}

function addDays(date: string, days: number): string {
  const d = new Date(`${date}T00:00:00Z`);
  d.setUTCDate(d.getUTCDate() + days);
  return d.toISOString().slice(0, 10);
}

/** Reports per calendar day (from the timestamp as written), gaps filled with zero between first and last day. */
export function countPerDay(
  reports: readonly Pick<Incident, "timestamp">[],
): CountItem[] {
  if (reports.length === 0) return [];
  const counts = new Map<string, number>();
  for (const r of reports) {
    const day = r.timestamp.slice(0, 10);
    counts.set(day, (counts.get(day) ?? 0) + 1);
  }
  const days = [...counts.keys()].sort();
  const result: CountItem[] = [];
  for (let day = days[0]; day <= days[days.length - 1]; day = addDays(day, 1)) {
    result.push({ label: day.slice(5), value: counts.get(day) ?? 0 });
  }
  return result;
}

export function countSessionsByStatus(
  sessions: readonly Pick<WalkSession, "status">[],
): Record<WalkSession["status"], number> {
  const result = { ACTIVE: 0, COMPLETED: 0, EMERGENCY: 0, ABANDONED: 0 };
  for (const s of sessions) result[s.status] += 1;
  return result;
}

export function emergencySummary(
  emergencies: readonly Pick<Emergency, "resolved">[],
) {
  const resolved = emergencies.filter((e) => e.resolved).length;
  return {
    total: emergencies.length,
    resolved,
    unresolved: emergencies.length - resolved,
  };
}
