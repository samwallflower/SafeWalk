import { ShieldIcon } from "lucide-react";

export function GuardNote() {
  return (
    <div className="flex gap-3 rounded-2xl bg-info-soft p-4 text-sm">
      <span className="h-fit rounded-full bg-card p-2 text-primary">
        <ShieldIcon className="size-4" aria-hidden="true" />
      </span>
      <div>
        <p className="font-bold">Community Guard Protocol</p>
        <p className="text-muted-foreground">
          Reports are reviewed by the community through upvotes and downvotes, and moderators can hide reports that look
          false. You can submit one report every few minutes.
        </p>
      </div>
    </div>
  );
}
