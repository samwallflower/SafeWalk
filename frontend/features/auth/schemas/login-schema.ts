import { z } from "zod";

/** Mirrors UserLoginRequest.java */
export const loginSchema = z.object({
  email: z.string().min(1, "Email is required").email("Email must be a valid email address").max(255),
  password: z.string().min(1, "Password is required"),
});

export type LoginValues = z.infer<typeof loginSchema>;
