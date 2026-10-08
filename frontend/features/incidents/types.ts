import type { IncidentCategory } from "@/features/categories/types";

export type ReportStatus = "ACTIVE" | "HIDDEN" | "UNDER_REVIEW";

/** Mirrors IncidentReportDto.java, including the reporter object the backend attaches. */
export interface IncidentReportDto {
  id: number;
  description: string;
  latitude: number;
  longitude: number;
  timestamp: string;
  isAnonymous: boolean;
  upvotes: number;
  downvotes: number;
  user: unknown;
  category: IncidentCategory;
  status: ReportStatus;
}

/** UI model. Carries only a display name for non-anonymous reports; no reporter contact details. */
export interface Incident {
  id: number;
  isAnonymous: boolean;
  /** "First L." for non-anonymous reports only. Never email or phone. */
  reporterName: string | null;
  description: string;
  latitude: number;
  longitude: number;
  timestamp: string;
  upvotes: number;
  downvotes: number;
  category: IncidentCategory;
  status: ReportStatus;
}

/** Mirrors HeatMapPointDto.java */
export interface HeatPoint {
  latitude: number;
  longitude: number;
  severityWeight: number;
}

/** Mirrors AddIncidentReportRequest.java (category is the entity; `name` is what the backend resolves). */
export interface CreateIncidentBody {
  description: string;
  latitude: number;
  longitude: number;
  isAnonymous: boolean;
  category: { id: number; name: string };
}
