package com.samwallflower.safewalk.service.routing;

import com.samwallflower.safewalk.dto.RouteDto;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.integration.googlemaps.GoogleMapsClient;
import com.samwallflower.safewalk.integration.googlemaps.GoogleRouteCandidate;
import com.samwallflower.safewalk.model.IncidentCategory;
import com.samwallflower.safewalk.model.IncidentReport;
import com.samwallflower.safewalk.model.Route;
import com.samwallflower.safewalk.repository.IncidentReportRepository;
import com.samwallflower.safewalk.repository.RouteRepository;
import com.samwallflower.safewalk.request.route.RouteRecommendationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoutingServiceTest {
    @Mock private GoogleMapsClient googleMapsClient;
    @Mock private RouteRepository routeRepository;
    @Mock private IncidentReportRepository incidentReportRepository;

    private RoutingService routingService;

    // Real encoded polyline (Google's own documented example):
    // decodes to (38.5,-120.2), (40.7,-120.95), (43.252,-126.453)
    private static final String REAL_POLYLINE = "_p~iF~ps|U_ulLnnqC_mqNvxq`@";

    @BeforeEach
    void setUp() {
        routingService = new RoutingService(googleMapsClient,routeRepository,
                new ModelMapper(), incidentReportRepository);

        ReflectionTestUtils.setField(routingService, "segmentBufferMeters", 100.0);
        ReflectionTestUtils.setField(routingService, "penaltyMetersPerSeverityPoint", 50.0);


    }

    private RouteRecommendationRequest buildRequest(){
        RouteRecommendationRequest request = new RouteRecommendationRequest();
        request.setOriginLatitude(47.4979);
        request.setOriginLongitude(21.6244);
        request.setDestinationLatitude(47.5316);
        request.setDestinationLongitude(21.6273);
        return request;
    }

    @Test
    void recommendRoutes_noIncidents_zeroPenalty_rankedByDistance(){
        GoogleRouteCandidate longer = new GoogleRouteCandidate(REAL_POLYLINE,1500.0);
        GoogleRouteCandidate shorter = new  GoogleRouteCandidate(REAL_POLYLINE,1000.0);

        when(googleMapsClient.getAlternativeRoutes(anyDouble(),anyDouble(),anyDouble(),anyDouble()))
                .thenReturn(List.of(longer, shorter)); // deliberately out of order

        when(incidentReportRepository.findNearBy(anyDouble(), anyDouble(),anyDouble()))
                .thenReturn(List.of());

        when(routeRepository.saveAll(anyList())).thenAnswer(invocationOnMock -> {
                    List<Route> routes = invocationOnMock.getArgument(0);
                    long id = 1L;
                    for (Route r : routes) r.setId(id++);
                    return routes;
                });

        List<RouteDto> result = routingService.recommendRoutes(buildRequest());

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getActualDistanceMeters()).isEqualTo(1000.0);
        assertThat(result.get(0).getSafetyPenaltyMeters()).isEqualTo(0.0);
        assertThat(result.get(0).getVirtualDistanceMeters()).isEqualTo(1000.0);
        assertThat(result.get(0).getRank()).isEqualTo(1);
        assertThat(result.get(1).getRank()).isEqualTo(2);
        // same request → same routeRequestId across siblings
        assertThat(result.get(0).getRouteRequestId()).isEqualTo(result.get(1).getRouteRequestId());

    }

    @Test
    void recommendRoutes_withIncidents_appliesSeverityPenalty() {
        GoogleRouteCandidate candidate = new GoogleRouteCandidate(REAL_POLYLINE, 1000.0);

        when(googleMapsClient.getAlternativeRoutes(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(candidate));

        IncidentCategory robbery = new IncidentCategory();
        robbery.setSeverityWeight(20);

        IncidentReport incident = new IncidentReport();
        incident.setId(1L);
        incident.setCategory(robbery);

        // every segment lookup returns the same incident — tests dedup + sum, not real geo filtering
        when(incidentReportRepository.findNearBy(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(incident));

        when(routeRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Route> routes = inv.getArgument(0);
            long id = 1L;
            for (Route r : routes) r.setId(id++);
            return routes;
        });

        List<RouteDto> result = routingService.recommendRoutes(buildRequest());

        assertThat(result.size()).isEqualTo(1);
        // same incident found on every segment, but counted ONCE due to dedup
        // -> penalty = 20 (severity) * 50 (meters/point) = 1000
        assertThat(result.get(0).getSafetyPenaltyMeters()).isEqualTo(1000.0);
        assertThat(result.get(0).getVirtualDistanceMeters()).isEqualTo(2000.0); // 1000 actual + 1000 penalty
    }

    @Test
    void recommendRoutes_dedupsIncidentAcrossMultipleSegments() {
        GoogleRouteCandidate candidate = new GoogleRouteCandidate(REAL_POLYLINE, 500.0);

        when(googleMapsClient.getAlternativeRoutes(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(candidate));

        IncidentCategory harassment = new IncidentCategory();
        harassment.setSeverityWeight(19);

        IncidentReport sameIncident = new IncidentReport();
        sameIncident.setId(42L);
        sameIncident.setCategory(harassment);

        // returned by findNearBy on EVERY segment call — simulates an incident
        // sitting near multiple consecutive segment midpoints
        when(incidentReportRepository.findNearBy(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(sameIncident));

        when(routeRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<RouteDto> result = routingService.recommendRoutes(buildRequest());

        // regardless of how many segments "found" it, it should only count once
        assertThat(result.get(0).getSafetyPenaltyMeters()).isEqualTo((19 * 50.0));
    }

    @Test
    void recommendRoutes_multipleDistinctIncidents_summedCorrectly() {
        GoogleRouteCandidate candidate = new GoogleRouteCandidate(REAL_POLYLINE, 500.0);

        when(googleMapsClient.getAlternativeRoutes(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(candidate));

        IncidentCategory robbery = new IncidentCategory();
        robbery.setSeverityWeight(20);
        IncidentReport incidentA = new IncidentReport();
        incidentA.setId(1L);
        incidentA.setCategory(robbery);

        IncidentCategory vandalism = new IncidentCategory();
        vandalism.setSeverityWeight(20);
        IncidentReport incidentB = new IncidentReport();
        incidentB.setId(2L);
        incidentB.setCategory(vandalism);

        when(incidentReportRepository.findNearBy(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(incidentA, incidentB));

        when(routeRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<RouteDto> result = routingService.recommendRoutes(buildRequest());

        // two distinct incidents, each counted once: (20 + 20) * 50 = 2000
        assertThat(result.get(0).getSafetyPenaltyMeters()).isEqualTo(2000.0);
    }


    @Test
    void getRouteById_returnsDto_whenFound() {
        Route route = new Route();
        route.setId(1L);
        route.setPolyline(REAL_POLYLINE);
        route.setActualDistanceMeters(500.0);
        route.setVirtualDistanceMeters(500.0);
        route.setSafetyPenaltyMeters(0.0);
        route.setRank(1);
        route.setRouteRequestId("req-1");

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        RouteDto dto = routingService.getRouteById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getPolyline()).isEqualTo(REAL_POLYLINE);
    }

    @Test
    void getRouteById_throwsResourceNotFound_whenMissing() {
        when(routeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routingService.getRouteById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getAllRoutes_returnsMappedList() {
        Route r1 = new Route();
        r1.setId(1L);
        r1.setPolyline(REAL_POLYLINE);
        Route r2 = new Route();
        r2.setId(2L);
        r2.setPolyline(REAL_POLYLINE);

        when(routeRepository.findAll()).thenReturn(List.of(r1, r2));

        List<RouteDto> result = routingService.getAllRoutes();

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting(RouteDto::getId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void getRouteByRouteRequestId_returnsMatchingRoutes() {
        Route r1 = new Route();
        r1.setId(1L);
        r1.setRouteRequestId("group-1");
        Route r2 = new Route();
        r2.setId(2L);
        r2.setRouteRequestId("group-1");

        when(routeRepository.findByRouteRequestId("group-1")).thenReturn(List.of(r1, r2));

        List<RouteDto> result = routingService.getRouteByRouteRequestId("group-1");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(dto -> dto.getRouteRequestId().equals("group-1"));
    }
}
