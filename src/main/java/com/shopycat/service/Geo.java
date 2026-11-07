package com.shopycat.service;

/** Small geography helpers. */
public final class Geo {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private Geo() {
    }

    /** Straight-line distance between two points in kilometres (haversine formula). */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** Rounds a coordinate to 2 decimal places, which hides the exact spot to within about 1 km. */
    public static double blur(double coordinate) {
        return Math.round(coordinate * 100.0) / 100.0;
    }
}
