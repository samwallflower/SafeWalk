export type VoteType = "UPVOTE" | "DOWNVOTE";

/** Mirrors backend-contract/java/dto/IncidentVoteDto.java */
export interface IncidentVoteDto {
  id: number;
  reportId: number;
  userId: number;
  voteType: VoteType;
}
