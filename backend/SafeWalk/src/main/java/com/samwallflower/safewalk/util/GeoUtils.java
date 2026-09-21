package com.samwallflower.safewalk.util;

public final class GeoUtils {
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private GeoUtils() {}
    //a = sin²(dLat/2) + cos(lat1) * cos(lat2) * sin²(dLon/2)
    public static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) *  Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Minimum distance from a point to a line segment, in meters
     * Approximates using equirectangular projection - accurate enough
     * at pedestrian scale ( tens to low hundreds of meters)
     */

    public static double distanceToSegmentMeters(
            double pointLat, double pointLon,
            double segStartLat, double segStartLon,
            double segEndLat, double segEndLon
    ){
        double x = pointLon , y = pointLat;
        double x1 = segStartLon, y1 = segStartLat;
        double x2 = segEndLon, y2 = segEndLat;

        double dx = x2 - x1;
        double dy = y2 - y1;

        if(dx == 0 && dy == 0){
            return haversineMeters(pointLat, pointLon, segStartLat, segStartLon);
        }

        double t = ((x-x1) * dx + (y-y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t)); // clamp to segment, not infinite line

        double projLon= x1 + t * dx;
        double projLat = y1 + t * dy;

        return haversineMeters(pointLat, pointLon, projLat, projLon);

    }
}
