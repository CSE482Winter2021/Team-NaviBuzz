package com.navisens.demo.android_app_helloworld.database_obj;

public class GPSPoint {
    private long longitude;
    private long latitude;
    private Landmark landmark; // optional field for a landmark. Only one per GPS point for now

    public GPSPoint(long latitude, long longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getLatitude() {
        return this.latitude;
    }

    public long getLongitude() {
        return this.longitude;
    }

    public Landmark getLandmark() {
        return landmark;
    }

    public void setLandmark(Landmark landmark) {
        this.landmark = landmark;
    }
}
