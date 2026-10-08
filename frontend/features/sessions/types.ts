export type SessionStatus = "ACTIVE" | "COMPLETED" | "EMERGENCY" | "ABANDONED";

/** Mirrors backend-contract/java/dto/WalkSessionDto.java */
export interface WalkSession {
  id: number;
  userId: number;
  routeId: number | null;
  startTime: string;
  endTime: string | null;
  originLatitude: number | null;
  originLongitude: number | null;
  destinationLatitude: number | null;
  destinationLongitude: number | null;
  lastKnownLatitude: number | null;
  lastKnownLongitude: number | null;
  lastLocationUpdate: string | null;
  lastArrivedAt: string | null;
  status: SessionStatus;
  alarmTriggered: boolean | null;
  autoCompleted: boolean | null;
  deviationTriggered: boolean | null;
  deviationTriggeredAt: string | null;
}
