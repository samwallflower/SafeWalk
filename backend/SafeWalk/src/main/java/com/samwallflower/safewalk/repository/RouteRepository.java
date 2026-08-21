package com.samwallflower.safewalk.repository;

import com.samwallflower.safewalk.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByPolylineIgnoreCase(String polyline);
    List<Route> findByPolylineContainingIgnoreCase(String polyline);
    List<Route> findByRouteRequestId(String routeRequestId);


}
