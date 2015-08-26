package com.example.jamin.charmander;

/**
 * Created by jamin on 8/25/15.
 */
public class RoutePoint {
    private double latitude;
    private double longitude;
    private float accuracy;
    private long utcTime;
    private int listener;

    public RoutePoint(double lat, double lng, float acc, long time, int list) {
        latitude = lat;
        longitude = lng;
        accuracy = acc;
        utcTime = time;
        listener = list;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public long getUTCTime() {
        return utcTime;
    }

    public int getListener() {
        return listener;
    }

}
