package com.navisens.demo.android_app_helloworld.database_obj;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(version = 2, entities= {Path.class, PathPoint.class})
public abstract class PathDatabase extends RoomDatabase {
    private static PathDatabase pathDB;

    abstract public PathDao getPathDao();
    abstract public PathPointDao getPathPointDao();

    public static PathDatabase getInstance(Context context) {
        if (null == pathDB) {
            // Good article on migrations:
            // https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
            // When we update version number it will reset tables, if we don't then it won't.
            pathDB = Room.databaseBuilder(context,
                    PathDatabase.class, "path_database").fallbackToDestructiveMigration().build();
        }
        return pathDB;
    }

    public void cleanUp(){
//        pathDB = null;
        getPathPointDao().deleteAll();
        getPathDao().deleteAll();
    }
}
