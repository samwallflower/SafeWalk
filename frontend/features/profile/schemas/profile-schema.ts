import { z } from "zod";

/** Mirrors UserUpdateRequest.java. Phone is optional; blank means "leave as is". */
export const profileSchema = z.object({
  firstName: z.string().trim().min(1, "First name is required").max(100, "First name must be under 100 characters"),
  lastName: z.string().trim().min(1, "Last name is required").max(100, "Last name must be under 100 characters"),
  phoneNumber: z
    .string()
    .trim()
    .refine((v) => v === "" || /^\+?[0-9\s\-()]{7,20}$/.test(v), "Phone number must be a valid phone number"),
});

export type ProfileValues = z.infer<typeof profileSchema>;
