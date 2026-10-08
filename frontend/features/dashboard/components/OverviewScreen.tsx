"use client";

import { FootprintsIcon, MegaphoneIcon } from "lucide-react";

import { SectionPanel } from "@/components/shared/SectionPanel";
import { MyReportsList } from "@/features/my-reports/components/MyReportsList";
import { SessionsList } from "@/features/sessions/components/SessionsList";

import { ContactsPanel } from "./ContactsPanel";
import { ProfileHeader } from "./ProfileHeader";
import { StatsGrid } from "./StatsGrid";

export function OverviewScreen() {
  return (
    <div className="space-y-6">
      <ProfileHeader />
      <StatsGrid />
      <div className="grid gap-6 lg:grid-cols-[minmax(0,3fr)_minmax(0,2fr)]">
        <div className="space-y-6">
          <SectionPanel
            title="Recent walk sessions"
            icon={<FootprintsIcon />}
            action={{ href: "/dashboard/sessions", label: "View all" }}
          >
            <SessionsList limit={3} />
          </SectionPanel>
          <SectionPanel
            title="My reported incidents"
            icon={<MegaphoneIcon />}
            action={{ href: "/dashboard/reports", label: "View all" }}
          >
            <MyReportsList limit={3} />
          </SectionPanel>
        </div>
        <ContactsPanel />
      </div>
    </div>
  );
}
