package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.IncidentVoteDto;
import com.samwallflower.safewalk.dto.PageResponse;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.incidentvote.IIncidentVoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/incident-votes")
public class IncidentVoteController {
    private final IIncidentVoteService incidentVoteService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllVotes(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentVoteDto> votes = incidentVoteService.getAllVotes(page, size);
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/report/{reportId}/vote")
    public ResponseEntity<ApiResponse> getVotesForReport(@PathVariable Long reportId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentVoteDto> votes = incidentVoteService.getVotesForReport(reportId,  page, size);
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
    public ResponseEntity<ApiResponse> getVotesByUserId(@PathVariable Long userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentVoteDto> votes = incidentVoteService.getVotesByUserId(userId, page, size);
        return ResponseEntity.ok(new ApiResponse("Votes retrieved successfully", votes));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/vote")
    public ResponseEntity<ApiResponse> getVoteById(@PathVariable Long id) {
        IncidentVoteDto voteDto = incidentVoteService.getVoteById(id);
        return ResponseEntity.ok(new ApiResponse("Vote retrieved successfully", voteDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-report-id-and-vote-type/report/{reportId}/vote")
    public ResponseEntity<ApiResponse> getVotesByReportIdAndVoteType(@PathVariable Long reportId, @RequestParam String voteType, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        PageResponse<IncidentVoteDto> votes = incidentVoteService.getVotesByReportIdAndVoteType(reportId, voteType, page, size);
        return ResponseEntity.ok(new ApiResponse("Votes retrieved successfully", votes));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count/by-report-and-vote-type/report/{reportId}/vote")
    public ResponseEntity<ApiResponse> countIncidentVotesByReportIdAndVoteType(@PathVariable Long reportId, @RequestParam String voteType) {
        long count = incidentVoteService.countIncidentVotesByReportIdAndVoteType(reportId, voteType);
        return ResponseEntity.ok(new ApiResponse("Vote count retrieved successfully", count));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/count/by-user-and-vote-type/user/{userId}/vote")
    public ResponseEntity<ApiResponse> countIncidentVotesByUserIdAndVoteType(@PathVariable Long userId, @RequestParam String voteType) {
        long count = incidentVoteService.countIncidentVotesByUserIdAndVoteType(userId, voteType);
        return ResponseEntity.ok(new ApiResponse("Vote count retrieved successfully", count));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count/by-vote-type/vote")
    public ResponseEntity<ApiResponse> countIncidentVotesByVoteType(@RequestParam String voteType) {
        long count = incidentVoteService.countIncidentVotesByVoteType(voteType);
        return ResponseEntity.ok(new ApiResponse("Vote count retrieved successfully", count));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count/all")
    public ResponseEntity<ApiResponse> countAllIncidentVotes(){
        long count = incidentVoteService.countAllIncidentVotes();
        return ResponseEntity.ok(new ApiResponse("Vote count retrieved successfully", count));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/count/all-by-user/user/{userId}/vote")
    public ResponseEntity<ApiResponse> countAllIncidentVotesByUserId(@PathVariable Long userId) {
        long count = incidentVoteService.countAllIncidentVotesByUserId(userId);
        return ResponseEntity.ok(new ApiResponse("Vote count retrieved successfully", count));
    }

}
