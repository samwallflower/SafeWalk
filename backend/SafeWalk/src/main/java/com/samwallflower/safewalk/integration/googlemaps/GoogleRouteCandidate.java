package com.samwallflower.safewalk.integration.googlemaps;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleRouteCandidate {
    private final String polyline;
    private final double actualDistanceMeters;
}
