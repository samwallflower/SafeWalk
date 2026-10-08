import { ShieldCheckIcon } from "lucide-react";

export function PrivacyNote() {
  return (
    <div className="flex gap-3 rounded-xl bg-muted p-4 text-sm">
      <ShieldCheckIcon className="mt-0.5 size-4 shrink-0 text-success" aria-hidden="true" />
      <div>
        <p className="font-semibold">Privacy first</p>
        <p className="text-muted-foreground">
          When reporting incidents you can post anonymously. Your account is only used to prevent spam and vote abuse.
        </p>
      </div>
    </div>
  );
}
