package com.samwallflower.safewalk.model;

import com.samwallflower.safewalk.enums.VoteType;

public class IncidentVote {

    private Long id;
    private IncidentReport report;
    private User user;
    private VoteType voteType; // upvote, downvote
}
