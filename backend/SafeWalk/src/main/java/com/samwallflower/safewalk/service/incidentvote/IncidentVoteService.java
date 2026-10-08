package com.samwallflower.safewalk.service.incidentvote;

import com.samwallflower.safewalk.dto.IncidentVoteDto;
import com.samwallflower.safewalk.dto.PageResponse;
import com.samwallflower.safewalk.enums.ReportStatus;
import com.samwallflower.safewalk.enums.VoteType;
import com.samwallflower.safewalk.exception.ResourceAlreadyExistsException;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.samwallflower.safewalk.model.IncidentReport;
import com.samwallflower.safewalk.model.IncidentVote;
import com.samwallflower.safewalk.model.User;
import com.samwallflower.safewalk.repository.IncidentReportRepository;
import com.samwallflower.safewalk.repository.IncidentVoteRepository;
import com.samwallflower.safewalk.repository.UserRepository;
import com.samwallflower.safewalk.security.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class IncidentVoteService implements IIncidentVoteService {
    private final IncidentVoteRepository incidentVoteRepository;
    private final UserRepository userRepository;
    private final IncidentReportRepository incidentReportRepository;
    private final Integer DOWNVOTE_THRESHOLD = 5;

    // first we make sure logged in user and the given user id are same
    // Users should not be able to cast vote on their own reports
    // after the vote has been cast we must also update the upvote and downvote number on the incident report
    @Override
    @Transactional
    public IncidentVoteDto castVote(Long userId, Long reportId, String voteType) {
        SecurityUtils.checkOwnershipOrAdmin(userId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User not found with id " + userId));

        IncidentReport incidentReport = incidentReportRepository.findById(reportId)
                .orElseThrow(()->new ResourceNotFoundException("Incident report not found with id " + reportId));

        if (incidentReport.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot vote on your own reports");
        }

        boolean alreadyVoted = incidentVoteRepository.existsByReportIdAndUserId(reportId, userId);
        if (alreadyVoted) {
            throw new ResourceAlreadyExistsException("You have already voted for this report");
        }

        VoteType vote = resolveVoteType(voteType);
        IncidentVote incidentVote = new IncidentVote();
        incidentVote.setUser(currentUser);
        incidentVote.setReport(incidentReport);
        incidentVote.setVoteType(vote);
        IncidentVote savedIncidentVote = incidentVoteRepository.save(incidentVote);

        if (vote == VoteType.UPVOTE) {
            incidentReport.setUpvotes(incidentReport.getUpvotes() + 1);
        } else {
            incidentReport.setDownvotes(incidentReport.getDownvotes() + 1);
            if(incidentReport.getDownvotes()>DOWNVOTE_THRESHOLD)
                incidentReport.setStatus(ReportStatus.HIDDEN);
        }

        incidentReportRepository.save(incidentReport);
        return convertToDto(savedIncidentVote);
    }
    // first we need to find the particular vote cast by the user for the report,
    // then we should recalculate the votes of the incident report and save it
    // then we can delete it from the repository
    @Override
    @Transactional
    public void removeVoteFromIncidentReport(Long reportId, Long userId) {
        SecurityUtils.checkOwnershipOrAdmin(userId);

        IncidentVote incidentVote = incidentVoteRepository.findByReportIdAndUserId(reportId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found for report id " + reportId + " and user id " + userId));

        if(!incidentVote.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only remove your own votes.");
        }

        reCalculateVotes(incidentVote);

    }

    private void reCalculateVotes(IncidentVote incidentVote) {
        IncidentReport report =  incidentVote.getReport();

        if (incidentVote.getVoteType() == VoteType.UPVOTE) {
            report.setUpvotes(Math.max(0, report.getUpvotes() - 1));
        } else {
            report.setDownvotes(Math.max(0, report.getDownvotes() - 1));
            if(report.getStatus() == ReportStatus.HIDDEN && report.getDownvotes() <= DOWNVOTE_THRESHOLD) {
                report.setStatus(ReportStatus.ACTIVE); // or whatever the default status is
            }
        }
        incidentReportRepository.save(report);
        incidentVoteRepository.delete(incidentVote);
    }

    @Override
    public long countIncidentVotes(Long reportId, String voteType) {
        return incidentVoteRepository.countByReportIdAndVoteType(reportId, resolveVoteType(voteType));
    }

    @Override
    public long countIncidentVotesByUserIdAndVoteType(Long userId, String voteType) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        return incidentVoteRepository.countIncidentVotesByUserIdAndVoteType(userId, resolveVoteType(voteType));
    }

    @Override
    public long countIncidentVotesByReportIdAndVoteType(Long reportId, String voteType) {
        return incidentVoteRepository.countIncidentVotesByReport_IdAndVoteType(reportId, resolveVoteType(voteType));
    }

    @Override
    public long countIncidentVotesByVoteType(String voteType) {
        return incidentVoteRepository.countIncidentVotesByVoteType(resolveVoteType(voteType));
    }

    @Override
    public long countAllIncidentVotes() {
        return incidentVoteRepository.count();
    }

    @Override
    public long countAllIncidentVotesByUserId(Long userId) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        return incidentVoteRepository.countAllByUserId(userId);
    }

    @Override
    public PageResponse<IncidentVoteDto> getVotesForReport(Long reportId, int page, int size) {
        return toPage(incidentVoteRepository.findByReportId(reportId, pageable(page, size)));
    }

    @Override
    public IncidentVoteDto getVoteById(Long id) {
        return incidentVoteRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found with id " + id));
    }

    // basically a list of all votes the user has cast
    @Override
    public PageResponse<IncidentVoteDto> getVotesByUserId(Long userId, int page, int size) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        return toPage(incidentVoteRepository.findByUserId(userId, pageable(page, size)));
    }

    @Override
    public IncidentVoteDto getVoteByReportIdAndUserId(Long reportId, Long userId) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        return incidentVoteRepository.findByReportIdAndUserId(reportId, userId)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found for report id " + reportId + " and user id " + userId));
    }

    @Override
    public PageResponse<IncidentVoteDto> getVotesByReportIdAndVoteType(Long reportId, String voteType, int page, int size) {
        return toPage(incidentVoteRepository.findByReportIdAndVoteType(reportId, resolveVoteType(voteType), pageable(page, size)));
    }

    // so for example - this particular user had previously cast upvote and now they want to change it to downvote
    // we change it and then we decrement the upvotes by one and increment the downvotes by one and save the incident report
    // vice versa for the other scenario
    // we must also update the report status based on the downvotes
    @Override
    @Transactional
    public IncidentVoteDto updateVote(Long reportId, Long userId, String voteType) {
        SecurityUtils.checkOwnershipOrAdmin(userId);
        return incidentVoteRepository.findByReportIdAndUserId(reportId, userId)
                .map(incidentVote -> {
                    VoteType vote = resolveVoteType(voteType);
                    if(incidentVote.getVoteType() == vote) {
                        throw new ResourceProcessingException("Vote type is the same as the existing vote type");
                    }
                    incidentVote.setVoteType(vote);
                    IncidentVote savedIncidentVote = incidentVoteRepository.save(incidentVote);
                    IncidentReport report = getIncidentReportResolved(incidentVote, vote);
                    incidentReportRepository.save(report);
                    return convertToDto(incidentVoteRepository.save(savedIncidentVote));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found for report id " + reportId + " and user id " + userId));
    }

    @Override
    public PageResponse<IncidentVoteDto> getAllVotes(int page, int size) {
        return toPage(incidentVoteRepository.findAll(pageable(page, size)));
    }

    @Override
    @Transactional
    public void removeIncidentVoteById(Long id) {
        IncidentVote incidentVote = incidentVoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found with id " + id));

        reCalculateVotes(incidentVote);
    }

    // basically delete all votes of a report
    @Override
    @Transactional
    public void removeIncidentVoteByReportId(Long reportId) {
        IncidentReport report = incidentReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident report not found with id " + reportId));
        report.setUpvotes(0);
        report.setDownvotes(0);

        incidentReportRepository.save(report);
        incidentVoteRepository.deleteByReportId(reportId);
    }


    private IncidentReport getIncidentReportResolved(IncidentVote incidentVote, VoteType vote) {
        IncidentReport report = incidentVote.getReport();
        if (vote == VoteType.UPVOTE) {
            report.setUpvotes(Math.max(0, report.getUpvotes() + 1));
            report.setDownvotes(Math.max(0, report.getDownvotes() - 1));
            if(report.getStatus() == ReportStatus.HIDDEN && report.getDownvotes() <= DOWNVOTE_THRESHOLD) {
                report.setStatus(ReportStatus.ACTIVE); // or whatever the default status is
            }
        }else{
            report.setDownvotes(Math.max(0, report.getDownvotes() + 1));
            report.setUpvotes(Math.max(0, report.getUpvotes() - 1));
            if (report.getDownvotes() > DOWNVOTE_THRESHOLD) {
                report.setStatus(ReportStatus.HIDDEN);
            }
        }
        return report;
    }

    @Override
    public IncidentVoteDto convertToDto(IncidentVote incidentVote) {
        IncidentVoteDto incidentVoteDto = new IncidentVoteDto();
        incidentVoteDto.setId(incidentVote.getId());
        incidentVoteDto.setUserId(incidentVote.getUser().getId());
        incidentVoteDto.setReportId(incidentVote.getReport().getId());
        incidentVoteDto.setVoteType(incidentVote.getVoteType());
        return incidentVoteDto;
    }

    private VoteType resolveVoteType(String voteType) {

        return switch (voteType.toLowerCase().trim()) {
            case "upvote","up" -> VoteType.UPVOTE;
            case "downvote","down" -> VoteType.DOWNVOTE;
            default -> throw new IllegalArgumentException("Invalid vote type: " + voteType);
        };

    }

    private PageResponse<IncidentVoteDto> toPage(Page<IncidentVote> page) {
        return PageResponse.from(page.map(this::convertToDto));
    }

    private Pageable pageable(int page, int size){
        return PageResponse.pageRequest(page, size);
    }
}
