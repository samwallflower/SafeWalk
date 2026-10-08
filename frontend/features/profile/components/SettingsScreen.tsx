"use client";

import { ErrorState } from "@/components/shared/ErrorState";
import { SectionPanel } from "@/components/shared/SectionPanel";
import { Skeleton } from "@/components/ui/skeleton";

import { useProfile } from "../hooks/useProfile";
import { ProfileForm } from "./ProfileForm";

export function SettingsScreen() {
  const profile = useProfile();
  return (
    <SectionPanel title="Profile">
      {profile.isPending ? (
        <div className="space-y-3" role="status" aria-label="Loading profile">
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
        </div>
      ) : profile.isError ? (
        <ErrorState message={profile.error.message} onRetry={() => void profile.refetch()} />
      ) : (
        <ProfileForm profile={profile.data} />
      )}
    </SectionPanel>
  );
}
