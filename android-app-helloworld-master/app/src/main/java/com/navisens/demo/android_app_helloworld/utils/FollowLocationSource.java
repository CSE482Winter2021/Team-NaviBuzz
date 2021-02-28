package com.navisens.demo.android_app_helloworld.utils;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import androidx.core.app.ActivityCompat;

public class FollowLocationSource implements LocationSource, LocationListener {
    private OnLocationChangedListener mListener;
    private LocationManager locationManager;
    private GoogleMap map;
    private Context context;
    private final Criteria criteria = new Criteria();
    private String bestAvailableProvider;
    /* Updates are restricted to one every 10 seconds, and only when
     * movement of more than 10 meters has been detected.*/
    private final int minTime = 10000;     // minimum time interval between location updates, in milliseconds
    private final int minDistance = 10;    // minimum distance between location updates, in meters

    public FollowLocationSource(Context context, GoogleMap map) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.map = map;

        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
    }

    public void getBestAvailableProvider() {
        bestAvailableProvider = locationManager.getBestProvider(criteria, true);
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;

        if (bestAvailableProvider != null) {
            locationManager.requestLocationUpdates(bestAvailableProvider, minTime, minDistance, this);
        }
    }

    /* Deactivates this provider.
     * This method is automatically invoked by disabling my-location layer. */
    @Override
    public void deactivate() {
        // Remove location updates from Location Manager
        locationManager.removeUpdates(this);

        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mListener != null) {
            mListener.onLocationChanged(location);
        }

        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
