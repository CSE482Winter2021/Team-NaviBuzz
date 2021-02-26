package com.navisens.demo.android_app_helloworld.utils;

import com.navisens.demo.android_app_helloworld.database_obj.CoordinatePoint;
import com.navisens.motiondnaapi.MotionDna;

public class Utils {
    // on longitude/latitude estimation without GPS, was noticing a consistent error on my phone.
    // adding a small bias fixes it, but should be tested on more phones
    private static final double BIAS = 0.000001;
    private static final double EARTH_RADIUS_KM = 6371.0088;

    // Setup database connection
    // Ret true on success
    public static boolean setupDatabase() {
        return true;
    }

    public static boolean postToDatabase(String sql) {
        return true;
    }

    public static boolean getFromDatabase(String sql) {
        return true;
    }

    /**
     * Given two points calculate the heading between them
     * @param x
     * @param y
     * @return
     */
    public static double getHeadingBetweenGPSPoints(CoordinatePoint x, CoordinatePoint y) {
        if (x == null || y == null) {
            return 0;
        }

        // Reference for this formula here https://www.igismap.com/formula-to-find-bearing-or-heading-angle-between-two-points-latitude-longitude/
        double delta = y.getLongitude() - x.getLongitude();
        double x1 = Math.cos(y.getLatitude())*Math.sin(Math.abs(delta));
        double y1 = Math.cos(x.getLatitude()) * Math.sin(y.getLatitude()) - Math.sin(x.getLatitude()) * Math.cos(y.getLatitude()) * Math.cos(delta);
        double heading = Math.toDegrees(Math.atan2(x1, y1));

        return heading;
    }

    /**
     * Estimate a new lat/lon given a lastLocation, distance traveled and heading
     *
     * Precondition is that the distance traveled was the same heading throughout
     *
     * @param lastLocation
     * @param lastCumulativeDistanceTraveled
     * @param heading
     * @return
     */
    public static CoordinatePoint estimateLongitudeLatitude(CoordinatePoint lastLocation, double lastCumulativeDistanceTraveled, double heading) {
        /*
         * Reference https://stackoverflow.com/questions/7222382/get-lat-long-given-current-point-distance-and-bearing
         */
        if (lastLocation == null || lastCumulativeDistanceTraveled == 0) {
            throw new IllegalArgumentException();
        }

        double hInRadians = Math.toRadians(heading);
        double dInkm = lastCumulativeDistanceTraveled / 1000;

        double longitudeInRadians = Math.toRadians(lastLocation.getLongitude());
        double latitudeInRadians = Math.toRadians(lastLocation.getLatitude());

        double newLat = Math.asin(Math.sin(latitudeInRadians) * Math.cos(dInkm / EARTH_RADIUS_KM) + Math.cos(latitudeInRadians) * Math.sin(dInkm / EARTH_RADIUS_KM) * Math.cos(hInRadians));
        double newLong = longitudeInRadians + Math.atan2(Math.sin(hInRadians) * Math.sin(dInkm / EARTH_RADIUS_KM) * Math.cos(latitudeInRadians), Math.cos(dInkm / EARTH_RADIUS_KM) - Math.sin(latitudeInRadians) * Math.sin(newLat));

        return new CoordinatePoint(Math.toDegrees(newLat) + BIAS, Math.toDegrees(newLong) + BIAS);
    }

    /**
     * Find distance between two points in meters
     *
     * @param pointOne
     * @param pointTwo
     * @return meters
     */
    public static double estimateDistanceBetweenTwoPoints(CoordinatePoint pointOne, CoordinatePoint pointTwo) {
        /*
         * Reference https://stackoverflow.com/questions/365826/calculate-distance-between-2-gps-coordinates
         */
        double vlat = Math.toRadians(pointTwo.getLatitude() - pointOne.getLongitude());
        double vlon = Math.toRadians(pointTwo.getLongitude() - pointOne.getLongitude());

        double latitude1 = Math.toRadians(pointOne.getLatitude());
        double latitude2 = Math.toRadians(pointTwo.getLatitude());

        double tmp = Math.sin(vlat / 2) * Math.sin(vlat / 2) + Math.sin(vlon / 2) * Math.sin(vlon / 2) * Math.cos(latitude1) * Math.cos(latitude2);
        return (EARTH_RADIUS_KM * (2 * Math.atan2(Math.sqrt(tmp), Math.sqrt(1-tmp)))) * 1000;
    }
}
