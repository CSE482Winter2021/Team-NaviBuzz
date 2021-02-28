package com.navisens.demo.android_app_helloworld.database_obj;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(version = 1, entities= {Path.class, PathPoint.class})
abstract class PathDatabase extends RoomDatabase {
    abstract public PathDao getPathDao();
    abstract public PathPointDao getPathPointDao();
}
