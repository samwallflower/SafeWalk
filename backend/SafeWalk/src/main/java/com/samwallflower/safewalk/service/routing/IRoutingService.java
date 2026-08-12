package com.samwallflower.safewalk.service.routing;

import com.samwallflower.safewalk.dto.RouteDto;
import com.samwallflower.safewalk.model.Route;
import com.samwallflower.safewalk.request.route.RouteRecommendationRequest;

import java.util.List;

public interface IRoutingService {
    List<RouteDto> getAllRoutes();

    List<RouteDto> recommendRoutes(RouteRecommendationRequest request);

    RouteDto getRouteById(Long routeId);

    List<RouteDto> getRouteByRouteRequestId(String routeRequestId);

    RouteDto convertToRouteDto(Route route);
}
