package com.samwallflower.safewalk.integration.googlemaps;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.LatLng;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleMapsClient {
    private final GeoApiContext  geoApiContext;

    public List<GoogleRouteCandidate> getAlternativeRoutes(
            double originLat, double originLng,
            double destinationLat, double destinationLng
    ){
        DirectionsResult result;
        try{
            result = DirectionsApi.newRequest(geoApiContext)
                    .origin(new LatLng(originLat,originLng))
                    .destination(new LatLng(destinationLat, destinationLng))
                    .alternatives(true)
                    .await();
        }catch(Exception e){
            log.error(e.getMessage());
            throw new ResourceProcessingException("Routing unavailable. Please try again later");
        }
        if(result.routes==null || result.routes.length==0){
            throw new ResourceProcessingException("No routes found between given locations.");
        }

        List<GoogleRouteCandidate> candidates = new ArrayList<>();
        for(DirectionsRoute route: result.routes){
            if(route.legs==null || route.legs.length==0){
                continue;
            }
            double distanceMeters = route.legs[0].distance.inMeters;
            String polyline = route.overviewPolyline.getEncodedPath();
            candidates.add(new GoogleRouteCandidate(polyline, distanceMeters));
        }
        if(candidates.isEmpty()){
            throw new ResourceProcessingException("No usable routes retuned by Google Maps.");
        }
        return candidates;
    }
}
