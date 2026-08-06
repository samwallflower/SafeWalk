package com.samwallflower.safewalk.repository;

import com.samwallflower.safewalk.enums.VoteType;
import com.samwallflower.safewalk.model.IncidentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentVoteRepository extends JpaRepository<IncidentVote, Long> {
    // basically what is the vote of this user on this report
    Optional<IncidentVote> findByReportIdAndUserId(Long reportId, Long userId);
    // all the votes for a specific report
    List<IncidentVote> findByReportId(Long reportId);
    List<IncidentVote> findByUserId(Long userId);
    // list of the all the upvotes or downvotes for a specific report
    List<IncidentVote> findByReportIdAndVoteType(Long reportId, VoteType voteType);
    // a way to count all the upvotes or downvotes for a report
    long countByReportIdAndVoteType(Long reportId, VoteType voteType);
}
