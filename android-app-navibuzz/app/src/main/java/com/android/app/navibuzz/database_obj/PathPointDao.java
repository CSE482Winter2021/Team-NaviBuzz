package com.android.app.navibuzz.database_obj;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PathPointDao {

    @Query ("SELECT * FROM points WHERE path_id = :p ORDER BY path_point_id ASC")
    public List<PathPoint> getByPathId(long p);

    @Query("SELECT  * FROM points WHERE path_id = :p GROUP BY path_point_id HAVING min(path_point_id)")
    public PathPoint getFirstPointByPathId(long p);

    @Insert
    public void addPathPoints(List<PathPoint> p);

    @Insert
    public void addPoint(PathPoint p);

    @Query("Update points SET landmark = :landmark WHERE path_point_id = :path_point_id")
    public void updateLandmark(long path_point_id, String landmark);

    @Query("Update points SET instruction = :instruction WHERE path_point_id = :path_point_id")
    public void updateInstruction(long path_point_id, String instruction);

    @Query("DELETE FROM points WHERE path_id = :path_id")
    public void deleteByPathId(long path_id);

    @Query("DELETE FROM points")
    public void deleteAll();


}
