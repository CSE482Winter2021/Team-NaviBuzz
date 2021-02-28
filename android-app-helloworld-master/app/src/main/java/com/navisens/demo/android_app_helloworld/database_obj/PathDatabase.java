package com.navisens.demo.android_app_helloworld.database_obj;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(version = 1, entities= {Path.class, PathPoint.class})
public abstract class PathDatabase extends RoomDatabase {
    private static PathDatabase pathDB;

    abstract public PathDao getPathDao();
    abstract public PathPointDao getPathPointDao();

    public static PathDatabase getInstance(Context context) {
        if (null == pathDB) {
            pathDB = Room.databaseBuilder(context,
                    PathDatabase.class, "path_database").build();
        }
        return pathDB;
    }

    public void cleanUp(){
        pathDB = null;
    }
}
