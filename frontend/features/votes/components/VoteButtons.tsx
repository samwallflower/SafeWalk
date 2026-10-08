"use client";

import { ThumbsDownIcon, ThumbsUpIcon } from "lucide-react";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

import { useVote } from "../hooks/useVote";
import type { VoteType } from "../types";

interface VoteButtonsProps {
  reportId: number;
  upvotes: number;
  downvotes: number;
}

export function VoteButtons({ reportId, upvotes, downvotes }: VoteButtonsProps) {
  const { canVote, myVote, isPending, vote } = useVote(reportId);

  const button = (type: VoteType, count: number, label: string, activeClass: string, icon: React.ReactNode) => (
    <Button
      type="button"
      variant="secondary"
      size="sm"
      className={cn("h-8 gap-1.5 px-2.5 font-semibold", myVote === type && activeClass)}
      aria-pressed={myVote === type}
      aria-label={`${label} (${count})`}
      disabled={!canVote || isPending}
      title={canVote ? label : "Log in to vote"}
      onClick={() => vote(type)}
    >
      {icon}
      {count}
    </Button>
  );

  return (
    <div className="flex items-center gap-2" aria-live="polite">
      {button("UPVOTE", upvotes, "Upvote", "bg-info-soft text-primary", <ThumbsUpIcon />)}
      {button("DOWNVOTE", downvotes, "Downvote", "bg-destructive-soft text-destructive", <ThumbsDownIcon />)}
    </div>
  );
}
