package com.samwallflower.safewalk.dto;

import com.samwallflower.safewalk.enums.VoteType;
import lombok.Data;

@Data
public class IncidentVoteDto {
    private Long id;
    private Long reportId;
    private Long userId;
    private VoteType voteType;
}
