package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.IncidentVoteDto;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.incidentvote.IIncidentVoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/incident-votes")
public class IncidentVoteController {
    private final IIncidentVoteService incidentVoteService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllVotes() {
        List<IncidentVoteDto> votes = incidentVoteService.getAllVotes();
        return ResponseEntity.ok(new ApiResponse("All votes retrieved successfully", votes));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/user/{userId}/report/{reportId}/cast")
    public ResponseEntity<ApiResponse> castVote(@PathVariable Long userId, @PathVariable Long reportId, @RequestParam String voteType) {
        IncidentVoteDto voteDto = incidentVoteService.castVote(userId, reportId, voteType);
        return ResponseEntity.ok(new ApiResponse( "Vote cast successfully", voteDto));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/user/{userId}/report/{reportId}/remove")
    public ResponseEntity<ApiResponse> removeVoteFromIncidentReport(@PathVariable Long reportId, @PathVariable Long userId) {
        incidentVoteService.removeVoteFromIncidentReport(reportId, userId);
        return ResponseEntity.ok(new ApiResponse("Vote removed successfully", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ApiResponse> removeIncidentVoteById(@PathVariable Long id) {
        incidentVoteService.removeIncidentVoteById(id);
        return ResponseEntity.ok(new ApiResponse("Vote removed successfully", null));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/user/{userId}/report/{reportId}/update")
    public ResponseEntity<ApiResponse> updateIncidentVote(@PathVariable Long userId, @PathVariable Long reportId, @RequestParam String voteType) {
        IncidentVoteDto voteDto = incidentVoteService.updateVote( reportId,userId, voteType);
        return ResponseEntity.ok(new ApiResponse("Vote updated successfully", voteDto));
    }


    @GetMapping("/report/{reportId}/count")
    public ResponseEntity<ApiResponse> countIncidentVotes(@PathVariable Long reportId, @RequestParam String voteType) {
        long count = incidentVoteService.countIncidentVotes(reportId, voteType);
        return ResponseEntity.ok(new ApiResponse("Vote count retrieved successfully", count));
    }

    @GetMapping("/report/{reportId}/vote")
    public ResponseEntity<ApiResponse> getVotesForReport(@PathVariable Long reportId) {
        List<IncidentVoteDto> votes = incidentVoteService.getVotesForReport(reportId);
        return ResponseEntity.ok(new ApiResponse("Votes retrieved successfully", votes));
    }

    // get vote of user on a specific report
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/user/{userId}/report/{reportId}/vote")
    public ResponseEntity<ApiResponse> getIncidentVote(@PathVariable Long userId, @PathVariable Long reportId) {
        IncidentVoteDto voteDto = incidentVoteService.getVoteByReportIdAndUserId(reportId, userId);
        return ResponseEntity.ok(new ApiResponse("Vote retrieved successfully", voteDto));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/user/{userId}/vote")
    public ResponseEntity<ApiResponse> getVotesByUserId(@PathVariable Long userId) {
        List<IncidentVoteDto> votes = incidentVoteService.getVotesByUserId(userId);
        return ResponseEntity.ok(new ApiResponse("Votes retrieved successfully", votes));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/vote")
    public ResponseEntity<ApiResponse> getVoteById(@PathVariable Long id) {
        IncidentVoteDto voteDto = incidentVoteService.getVoteById(id);
        return ResponseEntity.ok(new ApiResponse("Vote retrieved successfully", voteDto));
    }
}
