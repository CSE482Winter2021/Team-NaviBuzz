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
}
