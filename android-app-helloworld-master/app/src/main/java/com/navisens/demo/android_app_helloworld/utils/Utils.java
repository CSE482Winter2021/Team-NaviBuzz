package com.navisens.demo.android_app_helloworld.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

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

    //public static List<PathPoint> getPointsByPathIdFromDatabase(PathDatabase db, int pathId) {
        //return db.getPathPointDao().getByPathId(pathId);
    //}

    //public static List<Path> getUserPaths(PathDatabase db) {
        //return db.getPathDao().getAll();
    //}

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
        double delta = y.longitude - x.longitude;
        double x1 = Math.cos(y.latitude)*Math.sin(Math.abs(delta));
        double y1 = Math.cos(x.latitude) * Math.sin(y.latitude) - Math.sin(x.latitude) * Math.cos(y.latitude) * Math.cos(delta);
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

        double longitudeInRadians = Math.toRadians(lastLocation.longitude);
        double latitudeInRadians = Math.toRadians(lastLocation.latitude);

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
        double long2 = pointTwo.longitude;
        double long1 = pointOne.longitude;
        double lat2 = pointTwo.latitude;
        double lat1 = pointOne.latitude;


        double dlong = (long2 - long1) * d2r;
        double dlat = (lat2 - lat1) * d2r;
        double a = Math.pow(Math.sin(dlat/2.0), 2) + Math.cos(lat1*d2r) * Math.cos(lat2*d2r) * Math.pow(Math.sin(dlong/2.0), 2);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = 6367.0 * c * 1000.0;

        return d;
    }
}
