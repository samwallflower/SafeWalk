import { format, formatDistanceToNow, parseISO } from "date-fns";

/**
 * Backend timestamps are zone-less LocalDateTime strings. They are parsed as-is
 * (no second conversion) and only formatted for display.
 */
export function formatRelative(timestamp: string): string {
  return `${formatDistanceToNow(parseISO(timestamp))} ago`;
}

export function formatDateTime(timestamp: string): string {
  return format(parseISO(timestamp), "d MMM yyyy, HH:mm");
}

/** 850 -> "850 m", 2430 -> "2.4 km" */
export function formatDistance(meters: number): string {
  return meters < 1000 ? `${Math.round(meters)} m` : `${(meters / 1000).toFixed(1)} km`;
}

/** Signed difference, e.g. +120 m / -0.4 km */
export function formatDistanceDelta(meters: number): string {
  const sign = meters >= 0 ? "+" : "-";
  return `${sign}${formatDistance(Math.abs(meters))}`;
}

const WALKING_SPEED_M_PER_MIN = 5000 / 60;

/** Rough walking time at 5 km/h. An estimate only: the backend does not return durations. */
export function estimateWalkingMinutes(meters: number): number {
  return Math.max(1, Math.round(meters / WALKING_SPEED_M_PER_MIN));
}

/** Minutes between two zone-less timestamps, as "24 mins". Null when either is missing. */
export function formatDuration(start: string | null | undefined, end: string | null | undefined): string | null {
  if (!start || !end) return null;
  const minutes = Math.round((parseISO(end).getTime() - parseISO(start).getTime()) / 60_000);
  if (!Number.isFinite(minutes) || minutes < 0) return null;
  if (minutes < 60) return `${minutes} min${minutes === 1 ? "" : "s"}`;
  return `${Math.floor(minutes / 60)} h ${minutes % 60} min`;
}
