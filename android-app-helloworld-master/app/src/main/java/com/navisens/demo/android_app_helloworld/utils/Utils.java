package com.navisens.demo.android_app_helloworld.utils;

import com.navisens.demo.android_app_helloworld.database_obj.CoordinatePoint;
import com.navisens.motiondnaapi.MotionDna;

public class Utils {
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

        double newLat = Math.asin(Math.sin(latitudeInRadians) * Math.cos(dInkm / Constants.EARTH_RADIUS_KM) + Math.cos(latitudeInRadians) * Math.sin(dInkm / Constants.EARTH_RADIUS_KM) * Math.cos(hInRadians));
        double newLong = longitudeInRadians + Math.atan2(Math.sin(hInRadians) * Math.sin(dInkm / Constants.EARTH_RADIUS_KM) * Math.cos(latitudeInRadians), Math.cos(dInkm / Constants.EARTH_RADIUS_KM) - Math.sin(latitudeInRadians) * Math.sin(newLat));

        return new CoordinatePoint(Math.toDegrees(newLat) + Constants.BIAS, Math.toDegrees(newLong) + Constants.BIAS);
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
        return (Constants.EARTH_RADIUS_KM * (2 * Math.atan2(Math.sqrt(tmp), Math.sqrt(1-tmp)))) * 1000;
    }

    public static String getNewCoordinates(CoordinatePoint lastLocation, double distanceDifferential, MotionDna motionDna, boolean isGPSOn) {
        String str = "";
        /*
         * Check if GPS is on and if it is, use it
         *
         * if GPS is off, use estimation that updates every 40ms with dist + heading to
         * estimate the longitude/latitude. Long travels without GPS will add cumulatively more
         * error due to the 40ms latency with checking. This is then a beta stage feature until
         * this problem gets resolved or significantly reduced,
         * it's also not perfectly accurate ~10-20m off right now
         */
        if (isGPSOn) {
            str += "GPS is on \n";
            lastLocation.setLatitude(motionDna.getLocation().global.latitude);
            lastLocation.setLongitude(motionDna.getLocation().global.longitude);
        } else if (lastLocation.getLatitude() != 0 || lastLocation.getLongitude() != 0) {
            str += "GPS is off, using lat/long estimation";
            if (distanceDifferential > 0.4) {
                CoordinatePoint tmp = Utils.estimateLongitudeLatitude(lastLocation, distanceDifferential, motionDna.getLocation().global.heading);
                lastLocation.setLatitude(tmp.getLatitude());
                lastLocation.setLongitude(tmp.getLongitude());
            }
        } else {
            // This means GPS was never able to be used to get an initial location, this means
            // service unavailable because we can't estimate location
            str += "Service unavailable, GPS was never on";
        }

        return str;
    }
}
