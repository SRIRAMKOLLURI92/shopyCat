package com.shopycat.service;

/** A post together with its distance from the viewer (null when the viewer's location is unknown). */
public record Nearby<T>(T item, Double distanceKm) {

    public String distanceLabel() {
        if (distanceKm == null) {
            return "";
        }
        return distanceKm < 1 ? Math.round(distanceKm * 1000) + " m away" : String.format("%.1f km away", distanceKm);
    }
}
