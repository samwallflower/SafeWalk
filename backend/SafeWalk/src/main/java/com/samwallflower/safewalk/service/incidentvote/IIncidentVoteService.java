package com.samwallflower.safewalk.service.incidentvote;

import com.samwallflower.safewalk.dto.IncidentVoteDto;
import com.samwallflower.safewalk.enums.VoteType;
import com.samwallflower.safewalk.model.IncidentVote;

import java.util.List;

public interface IIncidentVoteService {

    IncidentVoteDto castVote(Long userId, Long reportId, String voteType);
    void removeVoteFromIncidentReport(Long reportId, Long userId);
    // how many upvotes / downvotes does a report have
    long countIncidentVotes(Long reportId, String voteType);

    List<IncidentVoteDto> getVotesForReport(Long reportId);
    IncidentVoteDto getVoteById(Long id);
    List<IncidentVoteDto> getVotesByUserId(Long userId);
    IncidentVoteDto getVoteByReportIdAndUserId(Long reportId, Long userId);

    IncidentVoteDto updateVote(Long reportId, Long userId, String voteType);
    List<IncidentVoteDto> getAllVotes();

    void removeIncidentVoteById(Long id);
    void removeIncidentVoteByReportId(Long reportId);

    IncidentVoteDto convertToDto(IncidentVote incidentVote);
}
