package com.samwallflower.safewalk.service.incidentvote;

import com.samwallflower.safewalk.dto.IncidentVoteDto;
import com.samwallflower.safewalk.dto.PageResponse;
import com.samwallflower.safewalk.model.IncidentVote;


public interface IIncidentVoteService {

    IncidentVoteDto castVote(Long userId, Long reportId, String voteType);
    void removeVoteFromIncidentReport(Long reportId, Long userId);
    // how many upvotes / downvotes does a report have
    long countIncidentVotes(Long reportId, String voteType);
    long countIncidentVotesByUserIdAndVoteType(Long userId, String voteType);
    long countIncidentVotesByReportIdAndVoteType(Long reportId, String voteType);
    long countIncidentVotesByVoteType(String voteType);
    long countAllIncidentVotes();
    long countAllIncidentVotesByUserId(Long userId);

    PageResponse<IncidentVoteDto> getVotesForReport(Long reportId, int page, int size);
    IncidentVoteDto getVoteById(Long id);
    PageResponse<IncidentVoteDto> getVotesByUserId(Long userId, int page, int size);
    IncidentVoteDto getVoteByReportIdAndUserId(Long reportId, Long userId);
    PageResponse<IncidentVoteDto> getVotesByReportIdAndVoteType(Long reportId, String voteType, int page, int size);

    IncidentVoteDto updateVote(Long reportId, Long userId, String voteType);
    PageResponse<IncidentVoteDto> getAllVotes(int page, int size);

    void removeIncidentVoteById(Long id);
    void removeIncidentVoteByReportId(Long reportId);

    IncidentVoteDto convertToDto(IncidentVote incidentVote);
}
