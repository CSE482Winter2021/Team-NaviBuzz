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
            // https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
            pathDB = Room.databaseBuilder(context,
                    PathDatabase.class, "path_database").fallbackToDestructiveMigration().build();
        }
        return pathDB;
    }

    public void deletePathId(long pid) {
        // Todo: add error checking
        pathDB.getPathPointDao().deleteByPathId(pid);
        pathDB.getPathDao().deletePath(pathDB.getPathDao().getById(pid));
    }

    public void cleanUp(){
//        pathDB = null;
        getPathPointDao().deleteAll();
        getPathDao().deleteAll();
    }
}
