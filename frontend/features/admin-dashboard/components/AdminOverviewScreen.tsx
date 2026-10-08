"use client";

import { useState } from "react";

import { PageHeader } from "@/components/shared/PageHeader";

import type { StatsWindow } from "../lib/admin-stats";
import { AdminStatsGrid } from "./AdminStatsGrid";
import { EmergenciesBySource } from "./EmergenciesBySource";
import { IncidentBreakdown } from "./IncidentBreakdown";
import { ReportsPerDay } from "./ReportsPerDay";
import { SessionsByStatus } from "./SessionsByStatus";
import { WindowToggle } from "./WindowToggle";

export function AdminOverviewScreen() {
  const [window, setWindow] = useState<StatsWindow>("7d");
  return (
    <div className="space-y-6">
      <PageHeader
        title="Safety operations"
        description="Platform activity, moderation and emergency oversight."
        actions={<WindowToggle value={window} onChange={setWindow} />}
      />
      <AdminStatsGrid window={window} />
      <div className="grid gap-6 lg:grid-cols-2">
        <IncidentBreakdown window={window} />
        <ReportsPerDay window={window} />
        <EmergenciesBySource />
        <SessionsByStatus />
      </div>
    </div>
  );
}
