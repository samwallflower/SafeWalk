package com.samwallflower.safewalk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samwallflower.safewalk.dto.RouteDto;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.request.route.RouteRecommendationRequest;
import com.samwallflower.safewalk.service.routing.IRoutingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoutingController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.prefix=/api/v1")
class RoutingControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private IRoutingService routingService;

    private RouteRecommendationRequest validRequest() {
        RouteRecommendationRequest request = new RouteRecommendationRequest();
        request.setOriginLatitude(47.4979);
        request.setOriginLongitude(21.6244);
        request.setDestinationLatitude(47.5316);
        request.setDestinationLongitude(21.6273);
        return request;
    }

    @Test
    void recommendRoutes_returns200_withRouteList() throws Exception {
        RouteDto dto = new RouteDto();
        dto.setId(1L);
        dto.setRank(1);
        dto.setSafetyPenaltyMeters(0.0);

        when(routingService.recommendRoutes(any())).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/v1/routing/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].rank").value(1));
    }

    @Test
    void recommendRoutes_returns400_whenLatitudeOutOfRange() throws Exception {
        RouteRecommendationRequest request = validRequest();
        request.setOriginLatitude(200.0); // invalid

        mockMvc.perform(post("/api/v1/routing/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recommendRoutes_returns400_whenFieldMissing() throws Exception {
        RouteRecommendationRequest request = validRequest();
        request.setDestinationLongitude(null);

        mockMvc.perform(post("/api/v1/routing/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRouteById_returns200_whenFound() throws Exception {
        RouteDto dto = new RouteDto();
        dto.setId(5L);

        when(routingService.getRouteById(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/routing/5/route"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));
    }

    @Test
    void getRouteById_returns404_whenNotFound() throws Exception {
        when(routingService.getRouteById(999L))
                .thenThrow(new ResourceNotFoundException("Could not find route with id: 999"));

        mockMvc.perform(get("/api/v1/routing/999/route"))
                .andExpect(status().isNotFound()); // adjust if your GlobalExceptionHandler maps differently
    }

    @Test
    void getAllRoutes_returns200() throws Exception {
        when(routingService.getAllRoutes()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/routing/all"))
                .andExpect(status().isOk());
    }

    @Test
    void getRoutesByRouteRequestId_returns200_withMatchingList() throws Exception {
        RouteDto dto = new RouteDto();
        dto.setRouteRequestId("group-1");

        when(routingService.getRouteByRouteRequestId("group-1")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/routing/by-route-request-id")
                        .param("routeRequestId", "group-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].routeRequestId").value("group-1"));
    }
}