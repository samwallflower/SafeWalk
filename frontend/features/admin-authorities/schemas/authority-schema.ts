import { z } from "zod";

const digits = (label: string) => z.string().trim().regex(/^\d{2,15}$/, `${label} must contain only digits (2 to 15 characters)`);

/** Mirrors AddEmergencyAuthorityRequest.java */
export const authoritySchema = z.object({
  countryCode: z.string().trim().regex(/^[A-Z]{2}$/, "Country code must be exactly two uppercase letters"),
  countryName: z.string().trim().min(2, "Country name must be between 2 and 100 characters").max(100, "Country name must be between 2 and 100 characters"),
  policeNumber: digits("Police number"),
  ambulanceNumber: digits("Ambulance number"),
  generalEmergencyNumber: z
    .string()
    .trim()
    .refine((v) => v === "" || /^\d{2,15}$/.test(v), "General emergency number must contain only digits (2 to 15 characters)"),
});

export type AuthorityValues = z.infer<typeof authoritySchema>;

/** Blank optional number is omitted from the request. */
export function toAuthorityBody(values: AuthorityValues) {
  const { generalEmergencyNumber, ...rest } = values;
  return generalEmergencyNumber ? { ...rest, generalEmergencyNumber } : rest;
}
