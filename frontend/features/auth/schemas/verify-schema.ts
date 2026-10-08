import { z } from "zod";

/** Mirrors VerifyUserRequest.java (backend declares no validation; code is required client-side). */
export const verifySchema = z.object({
  email: z.string().min(1, "Email is required").email("Email must be a valid email address"),
  verificationCode: z.string().trim().min(1, "Verification code is required"),
});

export type VerifyValues = z.infer<typeof verifySchema>;
