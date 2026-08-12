package com.samwallflower.safewalk.service.routing;

import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import com.samwallflower.safewalk.dto.RouteDto;
import com.samwallflower.safewalk.exception.ResourceNotFoundException;
import com.samwallflower.safewalk.integration.googlemaps.GoogleMapsClient;
import com.samwallflower.safewalk.integration.googlemaps.GoogleRouteCandidate;
import com.samwallflower.safewalk.model.IncidentReport;
import com.samwallflower.safewalk.model.Route;
import com.samwallflower.safewalk.repository.IncidentReportRepository;
import com.samwallflower.safewalk.repository.RouteRepository;
import com.samwallflower.safewalk.request.route.RouteRecommendationRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoutingService implements IRoutingService{
    private final GoogleMapsClient googleMapsClient;
    private final RouteRepository routeRepository;
    private final ModelMapper modelMapper;
    private final IncidentReportRepository incidentReportRepository;

    @Value("${app.routing.segment-buffer-meters}")
    private double segmentBufferMeters;
    @Value("${app.routing.penalty-meters-per-severity-point}")
    private double penaltyMetersPerSeverityPoint;

    @Override
    public List<RouteDto> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(this::convertToRouteDto)
                .toList();
    }

    /**
     * Calls Google Maps for alternative routes, computes the safety penalty
     * for each, persists all candidates as Route rows under a shared
     * routeRequestId and returns them ranked by virtual distance
     */

    @Override
    public List<RouteDto> recommendRoutes(RouteRecommendationRequest request) {
        String routeRequestId = UUID.randomUUID().toString();

        List<GoogleRouteCandidate> candidates = googleMapsClient.getAlternativeRoutes(
                request.getOriginLatitude(), request.getOriginLongitude(),
                request.getDestinationLatitude(), request.getDestinationLongitude()
        );
        List<Route> scoredRoutes = candidates.stream()
                .map(candidate -> buildRoute(candidate, routeRequestId))
                .sorted(Comparator.comparingDouble(Route::getVirtualDistanceMeters))
                .toList();

        // here we are setting ranks for the routes
        // the routes have been sorted based on virtual distance which includes the penalty
        for (int i = 0; i < scoredRoutes.size(); i++) {
            scoredRoutes.get(i).setRank(i+1);
        }

        List<Route> saved = routeRepository.saveAll(scoredRoutes);

        return saved.stream()
                .map(this::convertToRouteDto)
                .toList();
    }

    private Route buildRoute(GoogleRouteCandidate candidate, String routeRequestId) {
        double penalty = calculateSafetyPenalty(candidate.getPolyline());
        double virtualDistance = candidate.getActualDistanceMeters() + penalty;

        Route route = new Route();
        route.setPolyline(candidate.getPolyline());
        route.setActualDistanceMeters(candidate.getActualDistanceMeters());
        route.setSafetyPenaltyMeters(penalty);
        route.setVirtualDistanceMeters(virtualDistance);
        route.setRouteRequestId(routeRequestId);
        return route;
    }

    /**
     * basically we take a poly line and decode it first which returns us a list of points
     * each point has a lat and lng
     * then we make sure we don't count the same incident twice
     * we loop through all the points and their consecutive point
     * we find their mid-point
     * for each mid-point we call for nearby incident reports which are active
     * then we loop through the reports and
     * for each report we add the severity weight which we get from their category
     * finally we return the total severity weight multiplied by the penalty meters per severity point
     * this is how we calculate the safety penalty
     * */
    private double calculateSafetyPenalty(String polyline){
        List<LatLng> points = new EncodedPolyline(polyline).decodePath();

        System.out.println("Decoded " + points.size() + " points. First: " + points.get(0) + " Last: " + points.get(points.size()-1));

        // Dedup incidents across overlapping segment buffers so a single
        // incident near multiple consecutive midpoints isn't counted twice

        Set<Long> countedIncidents = new HashSet<>();
        double totalSeverityWeight = 0.0;

        for (int i = 0; i < points.size() - 1; i++) {
            LatLng start  = points.get(i);
            LatLng end = points.get(i + 1);
            double midLat = (start.lat+ end.lat) / 2.0;
            double midLng = (start.lng+ end.lng) / 2.0;

            List<IncidentReport> nearByReports = incidentReportRepository.findNearBy(midLat,midLng,segmentBufferMeters);
            System.out.println("Found: " + nearByReports.size());

            for (IncidentReport incidentReport : nearByReports) {
                if(countedIncidents.add(incidentReport.getId())){
                    totalSeverityWeight += incidentReport.getCategory().getSeverityWeight();
                }
            }

        }
        System.out.println("Buffer: " + segmentBufferMeters + ", Penalty/point: " + penaltyMetersPerSeverityPoint);

        return totalSeverityWeight * penaltyMetersPerSeverityPoint;
    }

    @Override
    public RouteDto getRouteById(Long routeId) {
        return routeRepository.findById(routeId)
                .map(this::convertToRouteDto)
                .orElseThrow(()-> new ResourceNotFoundException("Could not find route with id: " + routeId));
    }

    @Override
    public List<RouteDto> getRouteByRouteRequestId(String routeRequestId) {
        return routeRepository.findByRouteRequestId(routeRequestId)
                .stream()
                .map(this::convertToRouteDto)
                .toList();
    }

    @Override
    public RouteDto convertToRouteDto(Route route) {
        return modelMapper.map(route, RouteDto.class);
    }
}
