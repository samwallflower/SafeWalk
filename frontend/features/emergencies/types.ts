import type { WalkSession } from "@/features/sessions/types";

export type EmergencyTriggerSource = "MANUAL_SOS" | "IDLE_TIMEOUT" | "ROUTE_DEVIATION" | "CONNECTION_LOST" | "SYSTEM";

/** Mirrors backend-contract/java/dto/EmergencyDto.java (contact fields omitted: not shown on web). */
export interface Emergency {
  id: number;
  walkSession: WalkSession | null;
  triggerSource: EmergencyTriggerSource;
  triggerLatitude: number | null;
  triggerLongitude: number | null;
  triggerTimestamp: string;
  resolved: boolean;
  resolvedAt: string | null;
}
