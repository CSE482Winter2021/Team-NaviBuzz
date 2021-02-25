package com.navisens.demo.android_app_helloworld.database_obj;

import com.navisens.motiondnaapi.MotionDna;

public class CoordinatePoint {
    private double longitude;
    private double latitude;
    private Landmark landmark; // optional field for a landmark. Only one per GPS point for now

    public CoordinatePoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public Landmark getLandmark() {
        return landmark;
    }

    public void setLandmark(Landmark landmark) {
        this.landmark = landmark;
    }
}
