import { describe, expect, it } from "vitest";

import type { IncidentReportDto } from "../types";
import { toIncident } from "./incident-mappers";

const user = { id: 2, email: "secret@email.com", firstName: "Elena", lastName: "vance", phoneNumber: "123456" };

function dto(overrides: Partial<IncidentReportDto>): IncidentReportDto {
  return {
    id: 1,
    description: "d",
    latitude: 1,
    longitude: 2,
    timestamp: "2026-10-05T10:00:00",
    isAnonymous: true,
    upvotes: 0,
    downvotes: 0,
    user,
    category: { id: 1, name: "c", severityWeight: 1, description: null },
    status: "ACTIVE",
    ...overrides,
  };
}

describe("toIncident", () => {
  it("never exposes a name for anonymous reports", () => {
    expect(toIncident(dto({ isAnonymous: true })).reporterName).toBeNull();
  });

  it("shows 'First L.' for non-anonymous reports", () => {
    const incident = toIncident(dto({ isAnonymous: false }));
    expect(incident.isAnonymous).toBe(false);
    expect(incident.reporterName).toBe("Elena V.");
  });

  it("never keeps email or phone anywhere in the result", () => {
    const json = JSON.stringify(toIncident(dto({ isAnonymous: false })));
    expect(json).not.toContain("secret@email.com");
    expect(json).not.toContain("123456");
  });

  it("treats a missing flag as anonymous", () => {
    expect(toIncident({ ...dto({}), isAnonymous: undefined } as unknown as IncidentReportDto).reporterName).toBeNull();
  });
});
