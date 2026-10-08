import { isRecord } from "@/lib/http/envelope";

import type { Incident, IncidentReportDto } from "../types";

/** "Elena V." from the reporter object, or null. Email, phone and id are deliberately not read. */
function displayName(user: unknown): string | null {
  if (!isRecord(user)) return null;
  const first = typeof user.firstName === "string" ? user.firstName.trim() : "";
  const last = typeof user.lastName === "string" ? user.lastName.trim() : "";
  if (!first) return null;
  return last ? `${first} ${last.charAt(0).toUpperCase()}.` : first;
}

/**
 * The privacy boundary: the backend attaches the full reporter to every report, but the UI only
 * ever sees a display name, and only when the report is not anonymous.
 */
export function toIncident(dto: IncidentReportDto): Incident {
  const isAnonymous = dto.isAnonymous !== false;
  return {
    id: dto.id,
    isAnonymous,
    reporterName: isAnonymous ? null : displayName(dto.user),
    description: dto.description,
    latitude: dto.latitude,
    longitude: dto.longitude,
    timestamp: dto.timestamp,
    upvotes: dto.upvotes ?? 0,
    downvotes: dto.downvotes ?? 0,
    category: dto.category,
    status: dto.status,
  };
}
