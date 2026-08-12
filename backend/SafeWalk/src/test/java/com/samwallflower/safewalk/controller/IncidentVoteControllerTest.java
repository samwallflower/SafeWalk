package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.IncidentVoteDto;
import com.samwallflower.safewalk.enums.VoteType;
import com.samwallflower.safewalk.exception.ResourceAlreadyExistsException;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import com.samwallflower.safewalk.service.incidentvote.IIncidentVoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentVoteController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.prefix=/api/v1")
class IncidentVoteControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private IIncidentVoteService incidentVoteService;

    @Test
    void getAllVotes_returns200() throws Exception {
        when(incidentVoteService.getAllVotes()).thenReturn(List.of(new IncidentVoteDto()));

        mockMvc.perform(get("/api/v1/incident-votes/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void castVote_returns200_onSuccess() throws Exception {
        IncidentVoteDto dto = new IncidentVoteDto();
        dto.setId(1L);
        dto.setVoteType(VoteType.UPVOTE);

        when(incidentVoteService.castVote(1L, 10L, "up")).thenReturn(dto);

        mockMvc.perform(post("/api/v1/incident-votes/user/1/report/10/cast")
                        .param("voteType", "up"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.voteType").value("UPVOTE"));
    }

    @Test
    void castVote_returns500_whenVotingOnOwnReport() throws Exception {
        when(incidentVoteService.castVote(1L, 10L, "up"))
                .thenThrow(new ResourceProcessingException("You cannot vote on your own reports"));

        mockMvc.perform(post("/api/v1/incident-votes/user/1/report/10/cast")
                        .param("voteType", "up"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void castVote_returns409_whenAlreadyVoted() throws Exception {
        when(incidentVoteService.castVote(1L, 10L, "up"))
                .thenThrow(new ResourceAlreadyExistsException("You have already voted for this report"));

        mockMvc.perform(post("/api/v1/incident-votes/user/1/report/10/cast")
                        .param("voteType", "up"))
                .andExpect(status().isConflict());
    }

    @Test
    void castVote_returns400_whenVoteTypeInvalid() throws Exception {
        when(incidentVoteService.castVote(1L, 10L, "sideways"))
                .thenThrow(new IllegalArgumentException("Invalid vote type: sideways"));

        mockMvc.perform(post("/api/v1/incident-votes/user/1/report/10/cast")
                        .param("voteType", "sideways"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeVoteFromIncidentReport_returns200_onSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/incident-votes/user/1/report/10/remove"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void removeVoteFromIncidentReport_returns404_whenVoteNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Vote not found for report id 10 and user id 1"))
                .when(incidentVoteService).removeVoteFromIncidentReport(10L, 1L);

        mockMvc.perform(delete("/api/v1/incident-votes/user/1/report/10/remove"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateIncidentVote_returns200_onSuccess() throws Exception {
        IncidentVoteDto dto = new IncidentVoteDto();
        dto.setVoteType(VoteType.DOWNVOTE);

        when(incidentVoteService.updateVote(10L, 1L, "downvote")).thenReturn(dto);

        mockMvc.perform(put("/api/v1/incident-votes/user/1/report/10/update")
                        .param("voteType", "downvote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.voteType").value("DOWNVOTE"));
    }

    @Test
    void updateIncidentVote_returns500_whenSameVoteType() throws Exception {
        when(incidentVoteService.updateVote(10L, 1L, "upvote"))
                .thenThrow(new ResourceProcessingException("Vote type is the same as the existing vote type"));

        mockMvc.perform(put("/api/v1/incident-votes/user/1/report/10/update")
                        .param("voteType", "upvote"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void countIncidentVotes_returns200_withCount() throws Exception {
        when(incidentVoteService.countIncidentVotes(10L, "up")).thenReturn(5L);

        mockMvc.perform(get("/api/v1/incident-votes/report/10/count")
                        .param("voteType", "up"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    void getVotesForReport_returns200() throws Exception {
        when(incidentVoteService.getVotesForReport(10L)).thenReturn(List.of(new IncidentVoteDto()));

        mockMvc.perform(get("/api/v1/incident-votes/report/10/vote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getIncidentVote_returns200_withUserAndReportId() throws Exception {
        IncidentVoteDto dto = new IncidentVoteDto();
        dto.setUserId(1L);
        dto.setReportId(10L);

        when(incidentVoteService.getVoteByReportIdAndUserId(10L, 1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/incident-votes/user/1/report/10/vote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.reportId").value(10));
    }

    @Test
    void getVotesByUserId_returns200() throws Exception {
        when(incidentVoteService.getVotesByUserId(1L)).thenReturn(List.of(new IncidentVoteDto()));

        mockMvc.perform(get("/api/v1/incident-votes/user/1/vote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getVoteById_returns200() throws Exception {
        IncidentVoteDto dto = new IncidentVoteDto();
        dto.setId(50L);

        when(incidentVoteService.getVoteById(50L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/incident-votes/50/vote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(50));
    }

    @Test
    void getVoteById_returns404_whenNotFound() throws Exception {
        when(incidentVoteService.getVoteById(999L))
                .thenThrow(new ResourceNotFoundException("Vote not found with id 999"));

        mockMvc.perform(get("/api/v1/incident-votes/999/vote"))
                .andExpect(status().isNotFound());
    }
}