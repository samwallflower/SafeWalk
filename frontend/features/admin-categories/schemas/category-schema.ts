import { z } from "zod";

/** Mirrors AddIncidentCategoryRequest.java */
export const categorySchema = z.object({
  name: z.string().trim().min(1, "Name is required"),
  severityWeight: z
    .number({ error: "Severity must be a number" })
    .int("Severity must be a whole number")
    .min(1, "Severity weight must be at least 1")
    .max(20, "Severity weight must be at most 20"),
  description: z.string().trim(),
});

export type CategoryValues = z.infer<typeof categorySchema>;
