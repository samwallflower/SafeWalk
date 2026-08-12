package com.samwallflower.safewalk.controller;

import com.samwallflower.safewalk.dto.RouteDto;
import com.samwallflower.safewalk.request.route.RouteRecommendationRequest;
import com.samwallflower.safewalk.response.ApiResponse;
import com.samwallflower.safewalk.service.routing.IRoutingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/routing")
public class RoutingController {
    private final IRoutingService routingService;

    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse> recommendRoutes(@Valid @RequestBody RouteRecommendationRequest request){
        List<RouteDto> routes = routingService.recommendRoutes(request);
        return ResponseEntity.ok(new ApiResponse("Routes recommended successfully", routes));
    }

    @GetMapping("/{routeId}/route")
    public ResponseEntity<ApiResponse> getRouteById(@PathVariable Long routeId){
        RouteDto route = routingService.getRouteById(routeId);
        return ResponseEntity.ok(new ApiResponse("Route found successfully", route));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllRoutes(){
        List<RouteDto> routes = routingService.getAllRoutes();
        return ResponseEntity.ok(new ApiResponse("Routes found successfully", routes));
    }

    @GetMapping("/by-route-request-id")
    public ResponseEntity<ApiResponse> getRoutesByRouteRequestId(@RequestParam String routeRequestId){
        List<RouteDto> routes = routingService.getRouteByRouteRequestId(routeRequestId);
        return ResponseEntity.ok(new ApiResponse("Routes found successfully", routes));
    }
}
