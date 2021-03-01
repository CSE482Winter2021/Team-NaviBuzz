package com.navisens.demo.android_app_helloworld.utils;

import android.content.Context;

import com.navisens.demo.android_app_helloworld.database_obj.Path;
import com.navisens.demo.android_app_helloworld.database_obj.PathDatabase;
import com.navisens.demo.android_app_helloworld.database_obj.PathPoint;
import com.navisens.motiondnaapi.MotionDna;

import java.util.List;

public class Utils {
    // Setup database connection
    // Ret true on success

    public static PathDatabase setupDatabase(Context context) {
        return PathDatabase.getInstance(context);
    }

    public static void addPathToDatabase(PathDatabase db, List<PathPoint> points) {
        // once you have a db instance you can access the DAO object which has the methods
        // to update the tables
        db.getPathPointDao().addPathPoints(points);
    }

    public static List<PathPoint> getPointsByPathIdFromDatabase(PathDatabase db, int pathId) {
        return db.getPathPointDao().getByPathId(pathId);
    }

    public static List<Path> getUserPaths(PathDatabase db) {
        return db.getPathDao().getAll();
    }

    /**
     * Given two points calculate the heading between them
     * @param x
     * @param y
     * @return
     */
    public static double getHeadingBetweenGPSPoints(PathPoint x, PathPoint y) {
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
    public static PathPoint estimateLongitudeLatitude(PathPoint lastLocation, double lastCumulativeDistanceTraveled, double heading) {
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

        return new PathPoint(Math.toDegrees(newLat) + Constants.BIAS, Math.toDegrees(newLong) + Constants.BIAS);
    }

    /**
     * Find distance between two points in meters
     *
     * @param pointOne
     * @param pointTwo
     * @return meters
     */
    public static double estimateDistanceBetweenTwoPoints(PathPoint pointOne, PathPoint pointTwo) {
        /*
         * Reference https://stackoverflow.com/questions/365826/calculate-distance-between-2-gps-coordinates
         */
        double d2r = Math.PI / 180.0;
        double long2 = pointTwo.getLongitude();
        double long1 = pointOne.getLongitude();
        double lat2 = pointTwo.getLatitude();
        double lat1 = pointOne.getLatitude();


        double dlong = (long2 - long1) * d2r;
        double dlat = (lat2 - lat1) * d2r;
        double a = Math.pow(Math.sin(dlat/2.0), 2) + Math.cos(lat1*d2r) * Math.cos(lat2*d2r) * Math.pow(Math.sin(dlong/2.0), 2);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = 6367.0 * c * 1000.0;

        return d;
    }

    public static String getNewCoordinates(PathPoint lastLocation, double distanceDifferential, MotionDna motionDna, boolean isGPSOn) {
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
                lastLocation = new PathPoint(Utils.estimateLongitudeLatitude(lastLocation, distanceDifferential, motionDna.getLocation().global.heading));
            }
        } else {
            // This means GPS was never able to be used to get an initial location, this means
            // service unavailable because we can't estimate location
            str += "Service unavailable, GPS was never on";
        }

        return str;
    }
}
