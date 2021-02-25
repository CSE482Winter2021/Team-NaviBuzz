package com.navisens.demo.android_app_helloworld.database_obj;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PathPointDao {
    @Query ("SELECT * FROM points WHERE path_id = :p")
    public List<PathPoint> getByPathId(int p);

    @Insert
    public void addPathPoints(List<PathPoint> p);

    @Insert
    public void addPoint(PathPoint p);

    @Query("DELETE FROM points WHERE path_id = :path_id")
    public void deleteByPathId(int path_id);

    @Query("DELETE FROM points")
    public void deleteAll();
}
