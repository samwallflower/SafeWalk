package com.samwallflower.safewalk.integration.googlemaps;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.*;
import com.samwallflower.safewalk.exception.ResourceProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleMapsClient {
    private final GeoApiContext  geoApiContext;

    // Experiment mode: record/replay Directions responses so every configuration
    // sees identical alternatives and Google is called once per OD pair.
    @Value("${app.eval.enabled:false}")
    private boolean evalEnabled;
    @Value("${app.eval.directions-cache-dir:tools/data/cache/directions/replay}")
    private String cacheDir;
    private final ObjectMapper cacheMapper = new ObjectMapper();

    public List<GoogleRouteCandidate> getAlternativeRoutes(
            double originLat, double originLng,
            double destinationLat, double destinationLng
    ){
        if (!evalEnabled) {
            return fetchFromGoogle(originLat, originLng, destinationLat, destinationLng);
        }
        Path cacheFile = cacheFile(originLat, originLng, destinationLat, destinationLng);
        try {
            if (Files.exists(cacheFile)) {
                return cacheMapper.readValue(cacheFile.toFile(), new TypeReference<List<CachedRoute>>() {})
                        .stream().map(c -> new GoogleRouteCandidate(c.polyline(), c.distanceMeters())).toList();
            }
            List<GoogleRouteCandidate> fresh = fetchFromGoogle(originLat, originLng, destinationLat, destinationLng);
            Files.createDirectories(cacheFile.getParent());
            cacheMapper.writeValue(cacheFile.toFile(), fresh.stream()
                    .map(c -> new CachedRoute(c.getPolyline(), c.getActualDistanceMeters())).toList());
            return fresh;
        } catch (IOException e) {
            throw new ResourceProcessingException("Directions replay cache failed: " + e.getMessage());
        }
    }

    private record CachedRoute(String polyline, double distanceMeters) {}

    private Path cacheFile(double oLat, double oLng, double dLat, double dLng) {
        String name = String.format(Locale.ROOT, "walking_%.6f_%.6f_%.6f_%.6f.json", oLat, oLng, dLat, dLng);
        return Path.of(cacheDir, name);
    }

    private List<GoogleRouteCandidate> fetchFromGoogle(
            double originLat, double originLng,
            double destinationLat, double destinationLng
    ){
        DirectionsResult result;
        try{
            result = DirectionsApi.newRequest(geoApiContext)
                    .origin(new LatLng(originLat,originLng))
                    .destination(new LatLng(destinationLat, destinationLng))
                    .alternatives(true)
                    .mode(TravelMode.WALKING)
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

    public String reverseGeocodeCountryCode(double lat, double lng){
        try {
            GeocodingResult[] results = GeocodingApi.reverseGeocode(geoApiContext,new LatLng(lat,lng))
                    .await();

            if (results==null || results.length == 0) {
                throw new ResourceProcessingException("Could not resolve a location for the given coordinates.");
            }

            for(GeocodingResult result: results){
                for (AddressComponent component:result.addressComponents){
                    for (AddressComponentType type:component.types){
                        if (type==AddressComponentType.COUNTRY){
                            return component.shortName; //"HU"
                        }
                    }
                }
            }
            throw new ResourceProcessingException("Could not determine country for the given coordinates.");
        }catch (ResourceProcessingException e){
            throw e;
        } catch (Exception e) {
            log.error("Reverse geocoding failed {}", e.getMessage());
            throw new ResourceProcessingException("Reverse geocoding unavailable. Please try again later");
        }
    }
}
