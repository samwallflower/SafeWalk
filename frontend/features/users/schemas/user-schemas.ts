import { z } from "zod";

import { registerSchema } from "@/features/auth/schemas/register-schema";

/** Mirrors UserRegisterRequest.java (an admin adds people through the same endpoint as sign-up). */
export const createUserSchema = registerSchema;
export type CreateUserValues = z.infer<typeof createUserSchema>;

/** Mirrors UserUpdateRequest.java. The phone is optional, and blank means "leave it out". */
export const editUserSchema = z.object({
  firstName: z.string().trim().min(1, "First name is required").max(100, "First name must be under 100 characters"),
  lastName: z.string().trim().min(1, "Last name is required").max(100, "Last name must be under 100 characters"),
  phoneNumber: z
    .string()
    .trim()
    .refine((v) => v === "" || /^\+?[0-9\s\-()]{7,20}$/.test(v), "Enter a valid phone number"),
});
export type EditUserValues = z.infer<typeof editUserSchema>;
