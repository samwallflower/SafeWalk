"use client";

import {
  AlertTriangleIcon,
  FootprintsIcon,
  RouteIcon,
  UsersIcon,
  VoteIcon,
} from "lucide-react";
import { useMemo } from "react";

import { StatCard } from "@/components/shared/StatCard";
import { useContacts } from "@/features/contacts/hooks/useContacts";
import { useMyReports } from "@/features/my-reports/hooks/useMyReports";
import { useMySessions } from "@/features/sessions/hooks/useMySessions";
import { useMyVotes } from "@/features/votes/hooks/useMyVotes";
import { formatDistance } from "@/lib/format";

import { useDistanceWalked } from "../hooks/useDistanceWalked";
import { reportStats, sessionStats } from "../lib/user-stats";

export function StatsGrid() {
  const sessions = useMySessions();
  const reports = useMyReports();
  const votes = useMyVotes();
  const contacts = useContacts();
  const walked = useDistanceWalked();

  const s = useMemo(
    () => (sessions.data ? sessionStats(sessions.data) : null),
    [sessions.data],
  );
  const r = useMemo(
    () => (reports.data ? reportStats(reports.data) : null),
    [reports.data],
  );

  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-5">
      <StatCard
        label="Walk sessions"
        icon={<FootprintsIcon />}
        value={s ? String(s.total) : null}
        isLoading={sessions.isPending}
        isError={sessions.isError}
        detail={
          s
            ? `${s.completed} completed${s.emergencies ? ` • ${s.emergencies} emergency` : ""}`
            : null
        }
      />
      <StatCard
        label="Distance walked"
        icon={<RouteIcon />}
        value={formatDistance(walked.meters)}
        isLoading={walked.isPending}
        isError={walked.isError}
        detail={
          walked.walks === 0
            ? "Completed walks will appear here"
            : `${walked.walks} completed ${walked.walks === 1 ? "walk" : "walks"}${walked.capped ? " (most recent)" : ""}`
        }
      />
      <StatCard
        label="Incidents reported"
        icon={<AlertTriangleIcon />}
        value={r ? String(r.total) : null}
        isLoading={reports.isPending}
        isError={reports.isError}
        detail={r ? `${r.upvotes} upvotes • ${r.active} active` : null}
      />
      <StatCard
        label="Votes cast"
        icon={<VoteIcon />}
        value={votes.data !== undefined ? String(votes.data) : null}
        isLoading={votes.isPending}
        isError={votes.isError}
        detail="On other people's reports"
      />
      <StatCard
        label="Trusted contacts"
        icon={<UsersIcon />}
        value={contacts.data ? String(contacts.data.length) : null}
        isLoading={contacts.isPending}
        isError={contacts.isError}
        detail="Notified if a walk escalates"
      />
    </div>
  );
}
