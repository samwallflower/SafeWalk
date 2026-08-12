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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentVoteServiceTest {

    @Mock private IncidentVoteRepository incidentVoteRepository;
    @Mock private UserRepository userRepository;
    @Mock private IncidentReportRepository incidentReportRepository;

    @InjectMocks
    private IncidentVoteService service;

    @BeforeEach
    void setUp() {
        service = new IncidentVoteService(incidentVoteRepository, userRepository, incidentReportRepository);
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private IncidentReport buildReport(Long id, Long ownerId, int upvotes, int downvotes, ReportStatus status) {
        IncidentReport r = new IncidentReport();
        r.setId(id);
        r.setUser(buildUser(ownerId));
        r.setUpvotes(upvotes);
        r.setDownvotes(downvotes);
        r.setStatus(status);
        return r;
    }

    // ---------- castVote ----------

    @Test
    void castVote_throws_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.castVote(1L, 10L, "upvote"))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(incidentReportRepository);
    }

    @Test
    void castVote_throws_whenReportNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L)));
        when(incidentReportRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.castVote(1L, 10L, "upvote"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void castVote_throws_whenVotingOnOwnReport() {
        User user = buildUser(1L);
        IncidentReport report = buildReport(10L, 1L, 0, 0, ReportStatus.ACTIVE); // owned by same user

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(incidentReportRepository.findById(10L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.castVote(1L, 10L, "upvote"))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("own reports");

        verify(incidentVoteRepository, never()).save(any());
    }

    @Test
    void castVote_throws_whenAlreadyVoted() {
        User user = buildUser(1L);
        IncidentReport report = buildReport(10L, 2L, 0, 0, ReportStatus.ACTIVE); // different owner

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(incidentReportRepository.findById(10L)).thenReturn(Optional.of(report));
        when(incidentVoteRepository.existsByReportIdAndUserId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.castVote(1L, 10L, "upvote"))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(incidentVoteRepository, never()).save(any());
    }

    @Test
    void castVote_upvote_incrementsUpvoteCount() {
        User user = buildUser(1L);
        IncidentReport report = buildReport(10L, 2L, 3, 0, ReportStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(incidentReportRepository.findById(10L)).thenReturn(Optional.of(report));
        when(incidentVoteRepository.existsByReportIdAndUserId(10L, 1L)).thenReturn(false);
        when(incidentVoteRepository.save(any(IncidentVote.class))).thenAnswer(inv -> {
            IncidentVote v = inv.getArgument(0);
            v.setId(100L);
            return v;
        });

        IncidentVoteDto result = service.castVote(1L, 10L, "up");

        assertThat(result.getVoteType()).isEqualTo(VoteType.UPVOTE);
        assertThat(report.getUpvotes()).isEqualTo(4);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.ACTIVE);
        verify(incidentReportRepository).save(report);
    }

    @Test
    void castVote_downvote_belowThreshold_staysActive() {
        User user = buildUser(1L);
        IncidentReport report = buildReport(10L, 2L, 0, 3, ReportStatus.ACTIVE); // will become 4, threshold is >5

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(incidentReportRepository.findById(10L)).thenReturn(Optional.of(report));
        when(incidentVoteRepository.existsByReportIdAndUserId(10L, 1L)).thenReturn(false);
        when(incidentVoteRepository.save(any(IncidentVote.class))).thenAnswer(inv -> inv.getArgument(0));

        service.castVote(1L, 10L, "downvote");

        assertThat(report.getDownvotes()).isEqualTo(4);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.ACTIVE);
    }

    @Test
    void castVote_downvote_crossingThreshold_autoHides() {
        User user = buildUser(1L);
        IncidentReport report = buildReport(10L, 2L, 0, 5, ReportStatus.ACTIVE); // will become 6, threshold is >5

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(incidentReportRepository.findById(10L)).thenReturn(Optional.of(report));
        when(incidentVoteRepository.existsByReportIdAndUserId(10L, 1L)).thenReturn(false);
        when(incidentVoteRepository.save(any(IncidentVote.class))).thenAnswer(inv -> inv.getArgument(0));

        service.castVote(1L, 10L, "downvote");

        assertThat(report.getDownvotes()).isEqualTo(6);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.HIDDEN);
    }

    @Test
    void castVote_throws_whenVoteTypeInvalid() {
        User user = buildUser(1L);
        IncidentReport report = buildReport(10L, 2L, 0, 0, ReportStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(incidentReportRepository.findById(10L)).thenReturn(Optional.of(report));
        when(incidentVoteRepository.existsByReportIdAndUserId(10L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> service.castVote(1L, 10L, "sideways"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid vote type");
    }

    // ---------- removeVoteFromIncidentReport ----------

    @Test
    void removeVote_throws_whenVoteNotFound() {
        when(incidentVoteRepository.findByReportIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeVoteFromIncidentReport(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeVote_upvote_decrementsUpvoteCount_neverGoesNegative() {
        IncidentReport report = buildReport(10L, 2L, 0, 0, ReportStatus.ACTIVE); // already at 0
        IncidentVote vote = new IncidentVote();
        vote.setId(50L);
        vote.setVoteType(VoteType.UPVOTE);
        vote.setReport(report);

        when(incidentVoteRepository.findByReportIdAndUserId(10L, 1L)).thenReturn(Optional.of(vote));

        service.removeVoteFromIncidentReport(10L, 1L);

        assertThat(report.getUpvotes()).isEqualTo(0); // clamped, not -1
        verify(incidentVoteRepository).delete(vote);
    }

    @Test
    void removeVote_downvote_belowThreshold_restoresActiveStatus() {
        IncidentReport report = buildReport(10L, 2L, 0, 6, ReportStatus.HIDDEN); // will become 5, <=5 restores
        IncidentVote vote = new IncidentVote();
        vote.setId(50L);
        vote.setVoteType(VoteType.DOWNVOTE);
        vote.setReport(report);

        when(incidentVoteRepository.findByReportIdAndUserId(10L, 1L)).thenReturn(Optional.of(vote));

        service.removeVoteFromIncidentReport(10L, 1L);

        assertThat(report.getDownvotes()).isEqualTo(5);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.ACTIVE);
    }

    @Test
    void removeVote_downvote_stillAboveThreshold_staysHidden() {
        IncidentReport report = buildReport(10L, 2L, 0, 10, ReportStatus.HIDDEN); // becomes 9, still > 5
        IncidentVote vote = new IncidentVote();
        vote.setId(50L);
        vote.setVoteType(VoteType.DOWNVOTE);
        vote.setReport(report);

        when(incidentVoteRepository.findByReportIdAndUserId(10L, 1L)).thenReturn(Optional.of(vote));

        service.removeVoteFromIncidentReport(10L, 1L);

        assertThat(report.getDownvotes()).isEqualTo(9);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.HIDDEN);
    }

    // ---------- updateVote ----------

    @Test
    void updateVote_throws_whenSameVoteType() {
        IncidentReport report = buildReport(10L, 2L, 1, 0, ReportStatus.ACTIVE);
        IncidentVote existingVote = new IncidentVote();
        existingVote.setId(50L);
        existingVote.setVoteType(VoteType.UPVOTE);
        existingVote.setReport(report);

        when(incidentVoteRepository.findByReportIdAndUserId(10L, 1L)).thenReturn(Optional.of(existingVote));

        assertThatThrownBy(() -> service.updateVote(10L, 1L, "upvote"))
                .isInstanceOf(ResourceProcessingException.class)
                .hasMessageContaining("same as the existing");

        verify(incidentVoteRepository, never()).save(any());
    }

    @Test
    void updateVote_flipsUpvoteToDownvote_adjustsCountsAndThreshold() {
        IncidentReport report = buildReport(10L, 2L, 1, 5, ReportStatus.ACTIVE); // downvotes will become 6 -> HIDDEN
        IncidentVote existingVote = new IncidentVote();
        existingVote.setId(50L);
        existingVote.setUser(buildUser(1L));
        existingVote.setVoteType(VoteType.UPVOTE);
        existingVote.setReport(report);

        when(incidentVoteRepository.findByReportIdAndUserId(10L, 1L)).thenReturn(Optional.of(existingVote));
        when(incidentVoteRepository.save(any(IncidentVote.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidentVoteDto result = service.updateVote(10L, 1L, "downvote");

        assertThat(result.getVoteType()).isEqualTo(VoteType.DOWNVOTE);
        assertThat(report.getUpvotes()).isEqualTo(0);
        assertThat(report.getDownvotes()).isEqualTo(6);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.HIDDEN);
        verify(incidentReportRepository).save(report);
    }

    @Test
    void updateVote_flipsDownvoteToUpvote_restoresActiveWhenBelowThreshold() {
        IncidentReport report = buildReport(10L, 2L, 0, 6, ReportStatus.HIDDEN); // downvotes will become 5 -> restore
        IncidentVote existingVote = new IncidentVote();
        existingVote.setId(50L);
        existingVote.setUser(buildUser(1L));
        existingVote.setVoteType(VoteType.DOWNVOTE);
        existingVote.setReport(report);

        when(incidentVoteRepository.findByReportIdAndUserId(10L, 1L)).thenReturn(Optional.of(existingVote));
        when(incidentVoteRepository.save(any(IncidentVote.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidentVoteDto result = service.updateVote(10L, 1L, "upvote");

        assertThat(result.getVoteType()).isEqualTo(VoteType.UPVOTE);
        assertThat(report.getUpvotes()).isEqualTo(1);
        assertThat(report.getDownvotes()).isEqualTo(5);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.ACTIVE);
    }

    @Test
    void updateVote_throws_whenVoteNotFound() {
        when(incidentVoteRepository.findByReportIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateVote(10L, 1L, "upvote"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- removeIncidentVoteById (admin) ----------

    @Test
    void removeIncidentVoteById_throws_whenNotFound() {
        when(incidentVoteRepository.findById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeIncidentVoteById(50L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeIncidentVoteById_downvote_success() {
        IncidentReport report = buildReport(10L, 2L, 0, 3, ReportStatus.ACTIVE);
        IncidentVote vote = new IncidentVote();
        vote.setId(50L);
        vote.setVoteType(VoteType.DOWNVOTE);
        vote.setReport(report);

        when(incidentVoteRepository.findById(50L)).thenReturn(Optional.of(vote));

        service.removeIncidentVoteById(50L);

        assertThat(report.getDownvotes()).isEqualTo(2);
        verify(incidentVoteRepository).delete(vote);
    }

    // ---------- removeIncidentVoteByReportId ----------

    @Test
    void removeIncidentVoteByReportId_resetsCountsAndDeletesAllVotes() {
        IncidentReport report = buildReport(10L, 2L, 5, 3, ReportStatus.ACTIVE);
        IncidentVote v1 = new IncidentVote();
        v1.setId(1L);
        IncidentVote v2 = new IncidentVote();
        v2.setId(2L);

        when(incidentReportRepository.findById(10L)).thenReturn(Optional.of(report));
        when(incidentVoteRepository.findByReportId(10L)).thenReturn(List.of(v1, v2));

        service.removeIncidentVoteByReportId(10L);

        assertThat(report.getUpvotes()).isEqualTo(0);
        assertThat(report.getDownvotes()).isEqualTo(0);
        verify(incidentReportRepository).save(report);
        verify(incidentVoteRepository).deleteAll(List.of(v1, v2));
    }

    @Test
    void removeIncidentVoteByReportId_throws_whenReportNotFound() {
        when(incidentReportRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeIncidentVoteByReportId(10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- resolveVoteType via public methods ----------

    @Test
    void countIncidentVotes_resolvesVoteTypeAliases() {
        when(incidentVoteRepository.countByReportIdAndVoteType(10L, VoteType.UPVOTE)).thenReturn(7L);

        long count = service.countIncidentVotes(10L, "Up"); // mixed case alias

        assertThat(count).isEqualTo(7L);
    }

    @Test
    void countIncidentVotes_throws_whenVoteTypeInvalid() {
        assertThatThrownBy(() -> service.countIncidentVotes(10L, "invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------- read paths ----------

    @Test
    void getVoteById_throws_whenNotFound() {
        when(incidentVoteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getVoteById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getVotesForReport_mapsToDto() {
        IncidentVote vote = new IncidentVote();
        vote.setId(1L);
        vote.setUser(buildUser(2L));
        vote.setReport(buildReport(10L, 3L, 0, 0, ReportStatus.ACTIVE));
        vote.setVoteType(VoteType.UPVOTE);

        when(incidentVoteRepository.findByReportId(10L)).thenReturn(List.of(vote));

        List<IncidentVoteDto> result = service.getVotesForReport(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(2L);
        assertThat(result.get(0).getReportId()).isEqualTo(10L);
    }
}