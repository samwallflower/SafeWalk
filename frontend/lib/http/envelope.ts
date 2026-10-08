/** Shape of every controller response: `{ message, data }`. */
export interface ApiEnvelope<T> {
  message: string;
  data: T | null;
}

/** Security-handler errors (401/403) also carry `message`, plus extra fields. */
export interface ApiErrorBody {
  message?: string;
  error?: string;
  status?: number;
}

export function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}
