import { z } from "zod";

/** Mirrors UserRegisterRequest.java */
export const registerSchema = z.object({
  email: z.string().min(1, "Email is required").email("Email must be a valid email address").max(255),
  password: z
    .string()
    .min(8, "Password must be between 8 and 72 characters")
    .max(72, "Password must be between 8 and 72 characters")
    .regex(/^(?=.*[A-Za-z])(?=.*\d).+$/, "Password must contain at least one letter and one number"),
  firstName: z.string().min(1, "First name is required").max(100),
  lastName: z.string().min(1, "Last name is required").max(100),
});

export type RegisterValues = z.infer<typeof registerSchema>;
