import { ThumbsDownIcon, ThumbsUpIcon } from "lucide-react";

interface VoteCountsProps {
  upvotes: number;
  downvotes: number;
}

const pill = "flex items-center gap-1.5 rounded-lg bg-muted px-2.5 py-1 text-sm font-semibold";

/** Read-only counts. Casting votes arrives in Phase D. */
export function VoteCounts({ upvotes, downvotes }: VoteCountsProps) {
  return (
    <div className="flex items-center gap-2" aria-live="polite">
      <span className={`${pill} text-primary`}>
        <ThumbsUpIcon className="size-4" aria-hidden="true" />
        <span className="sr-only">Upvotes:</span>
        {upvotes}
      </span>
      <span className={`${pill} text-muted-foreground`}>
        <ThumbsDownIcon className="size-4" aria-hidden="true" />
        <span className="sr-only">Downvotes:</span>
        {downvotes}
      </span>
    </div>
  );
}
