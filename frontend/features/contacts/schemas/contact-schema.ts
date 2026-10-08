import { z } from "zod";

/** Mirrors AddEmergencyContactRequest.java: phone optional but E.164 when given. */
export const contactSchema = z.object({
  contactName: z.string().trim().min(1, "Contact name is required").max(100, "Contact name must be under 100 characters"),
  contactEmail: z
    .string()
    .trim()
    .min(1, "Contact email is required")
    .max(255, "Contact email must be under 255 characters")
    .email("Contact email must be a valid email address"),
  contactPhone: z
    .string()
    .trim()
    .refine((v) => v === "" || /^\+[1-9]\d{6,14}$/.test(v), "Phone must be in international format, e.g. +36301234567"),
});

export type ContactValues = z.infer<typeof contactSchema>;

/** Blank phone is omitted so the backend treats it as "not provided". */
export function toContactBody(values: ContactValues) {
  return {
    contactName: values.contactName,
    contactEmail: values.contactEmail,
    ...(values.contactPhone ? { contactPhone: values.contactPhone } : {}),
  };
}
