import type { EmergencyTriggerSource } from "../types";

export const TRIGGER_SOURCES: readonly EmergencyTriggerSource[] = [
  "MANUAL_SOS",
  "IDLE_TIMEOUT",
  "ROUTE_DEVIATION",
  "CONNECTION_LOST",
  "SYSTEM",
];

export const SOURCE_LABEL: Record<EmergencyTriggerSource, string> = {
  MANUAL_SOS: "Manual SOS",
  IDLE_TIMEOUT: "Inactivity check timed out",
  ROUTE_DEVIATION: "Left the planned route",
  CONNECTION_LOST: "Connection lost",
  SYSTEM: "Triggered by the system",
};
