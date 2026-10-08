import { z } from "zod";

export const DESCRIPTION_MAX = 280;

/**
 * Mirrors AddIncidentReportRequest.java (latitude/longitude ranges). The backend has no description
 * rules; the 280 limit comes from the design.
 */
export const reportSchema = z.object({
  categoryId: z.number({ error: "Select a category" }),
  description: z
    .string()
    .trim()
    .min(1, "Describe what you observed")
    .max(DESCRIPTION_MAX, `Keep it under ${DESCRIPTION_MAX} characters`),
  latitude: z.number().min(-90, "Latitude must be between -90 and 90").max(90, "Latitude must be between -90 and 90"),
  longitude: z
    .number()
    .min(-180, "Longitude must be between -180 and 180")
    .max(180, "Longitude must be between -180 and 180"),
  isAnonymous: z.boolean(),
});

export type ReportValues = z.infer<typeof reportSchema>;
