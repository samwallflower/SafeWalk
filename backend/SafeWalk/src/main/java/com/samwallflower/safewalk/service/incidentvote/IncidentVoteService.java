package com.samwallflower.safewalk.service.incidentvote;

import com.samwallflower.safewalk.dto.IncidentVoteDto;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentVoteService implements IIncidentVoteService {
    private final IncidentVoteRepository incidentVoteRepository;
    private final UserRepository userRepository;
    private final IncidentReportRepository incidentReportRepository;

    // Users should not be able to cast vote on their own reports
    // after the vote has been cast we must also update the upvote and downvote number on the incident report
    @Override
    @Transactional
    public IncidentVoteDto castVote(Long userId, Long reportId, String voteType) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User not found with id " + userId));

        IncidentReport incidentReport = incidentReportRepository.findById(reportId)
                .orElseThrow(()->new ResourceNotFoundException("Incident report not found with id " + reportId));

        if (incidentReport.getUser().getId().equals(userId)) {
            throw new ResourceProcessingException("You cannot vote on your own reports");
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
            if(incidentReport.getDownvotes()>5)
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
        IncidentVote incidentVote = incidentVoteRepository.findByReportIdAndUserId(reportId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found for report id " + reportId + " and user id " + userId));

        IncidentReport report =  incidentVote.getReport();

        if (incidentVote.getVoteType() == VoteType.UPVOTE) {
            report.setUpvotes(Math.max(0, report.getUpvotes() - 1));
        } else {
            report.setDownvotes(Math.max(0, report.getDownvotes() - 1));
            if(report.getStatus() == ReportStatus.HIDDEN && report.getDownvotes() <= 5) {
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
    public List<IncidentVoteDto> getVotesForReport(Long reportId) {
        return incidentVoteRepository.findByReportId(reportId).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public IncidentVoteDto getVoteById(Long id) {
        return incidentVoteRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found with id " + id));
    }

    // basically a list of all votes the user has cast
    @Override
    public List<IncidentVoteDto> getVotesByUserId(Long userId) {
        return incidentVoteRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public IncidentVoteDto getVoteByReportIdAndUserId(Long reportId, Long userId) {
        return incidentVoteRepository.findByReportIdAndUserId(reportId, userId)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found for report id " + reportId + " and user id " + userId));
    }

    // so for example - this particular user had previously cast upvote and now they want to change it to downvote
    // we change it and then we decrement the upvotes by one and increment the downvotes by one and save the incident report
    // vice versa for the other scenario
    // we must also update the report status based on the downvotes
    @Override
    @Transactional
    public IncidentVoteDto updateVote(Long reportId, Long userId, String voteType) {
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
    public List<IncidentVoteDto> getAllVotes() {
        return incidentVoteRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional
    public void removeIncidentVoteById(Long id) {
        IncidentVote incidentVote = incidentVoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found with id " + id));

        IncidentReport report = incidentVote.getReport();

        if (incidentVote.getVoteType() == VoteType.UPVOTE) {
            report.setUpvotes(Math.max(0, report.getUpvotes() - 1));
        } else {
            report.setDownvotes(Math.max(0, report.getDownvotes() - 1));
            if(report.getStatus() == ReportStatus.HIDDEN && report.getDownvotes() <= 5) {
                report.setStatus(ReportStatus.ACTIVE); // or whatever the default status is
            }
        }
        incidentReportRepository.save(report);
        incidentVoteRepository.delete(incidentVote);
    }

    // basically delete all votes of a report
    @Override
    public void removeIncidentVoteByReportId(Long reportId) {
        IncidentReport report = incidentReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident report not found with id " + reportId));
        List<IncidentVote> incidentVotes = incidentVoteRepository.findByReportId(reportId);

        report.setUpvotes(0);
        report.setDownvotes(0);

        incidentReportRepository.save(report);
        incidentVoteRepository.deleteAll(incidentVotes);
    }


    private IncidentReport getIncidentReportResolved(IncidentVote incidentVote, VoteType vote) {
        IncidentReport report = incidentVote.getReport();
        if (vote == VoteType.UPVOTE) {
            report.setUpvotes(Math.max(0, report.getUpvotes() + 1));
            report.setDownvotes(Math.max(0, report.getDownvotes() - 1));
            if(report.getStatus() == ReportStatus.HIDDEN && report.getDownvotes() <= 5) {
                report.setStatus(ReportStatus.ACTIVE); // or whatever the default status is
            }
        }else{
            report.setDownvotes(Math.max(0, report.getDownvotes() + 1));
            report.setUpvotes(Math.max(0, report.getUpvotes() - 1));
            if (report.getDownvotes() > 5) {
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
}
