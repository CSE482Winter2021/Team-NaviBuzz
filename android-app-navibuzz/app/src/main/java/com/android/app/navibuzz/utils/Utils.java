package com.android.app.navibuzz.utils;

import android.content.Context;

import com.android.app.navibuzz.database_obj.PathPoint;
import com.android.app.navibuzz.database_obj.PathDatabase;

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
            throw new IllegalArgumentException();
        }

        double Phi1 = Math.toRadians(x.latitude);
        double Phi2 = Math.toRadians(y.latitude);
        double DeltaLambda = Math.toRadians(y.longitude - x.longitude);

        double Theta = Math.atan2((Math.sin(DeltaLambda)*Math.cos(Phi2)) , (Math.cos(Phi1)*Math.sin(Phi2) - Math.sin(Phi1)*Math.cos(Phi2)*Math.cos(DeltaLambda)));
        return (float)Math.toDegrees(Theta);
    }

    /**
     * This function, given a heading between two GPS points and a device orientation, returns
     * how many degrees the user needs to turn
     *
     * If a negative number, this means turn left. If positive turn right
     */
    public static double getHeadingTurnDegrees(double deviceHeading, double targetHeading) {
        // Reference https://math.stackexchange.com/questions/110080/shortest-way-to-achieve-target-angle
        return (targetHeading - deviceHeading + 540) % 360 - 180;
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
