import { z } from "zod";

import { DESCRIPTION_MAX } from "@/features/report/schemas/report-schema";

/** Location is not editable here; description, category and anonymity are. */
export const editReportSchema = z.object({
  categoryId: z.number({ error: "Select a category" }),
  description: z
    .string()
    .trim()
    .min(1, "Describe what you observed")
    .max(DESCRIPTION_MAX, `Keep it under ${DESCRIPTION_MAX} characters`),
  isAnonymous: z.boolean(),
});

export type EditReportValues = z.infer<typeof editReportSchema>;
