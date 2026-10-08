package com.samwallflower.safewalk.repository;

import com.samwallflower.safewalk.enums.VoteType;
import com.samwallflower.safewalk.model.IncidentVote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IncidentVoteRepository extends JpaRepository<IncidentVote, Long> {
    // basically what is the vote of this user on this report
    Optional<IncidentVote> findByReportIdAndUserId(Long reportId, Long userId);
    // all the votes for a specific report
    Page<IncidentVote> findByReportId(Long reportId, Pageable pageable);
    Page<IncidentVote> findByUserId(Long userId, Pageable pageable);
    // list of the all the upvotes or downvotes for a specific report
    Page<IncidentVote> findByReportIdAndVoteType(Long reportId, VoteType voteType,  Pageable pageable);
    // a way to count all the upvotes or downvotes for a report
    long countByReportIdAndVoteType(Long reportId, VoteType voteType);

    // a method to check if the user voted for a particular report
    boolean existsByReportIdAndUserId(Long reportId, Long userId);

    void deleteByReportId(Long reportId);

    long countIncidentVotesByReport_IdAndVoteType(Long reportId, VoteType voteType);

    long countIncidentVotesByVoteType(VoteType voteType);

    long countIncidentVotesByUserIdAndVoteType(Long userId, VoteType voteType);

    long countAllByUserId(Long userId);
}
