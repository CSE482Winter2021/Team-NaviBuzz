package com.navisens.demo.android_app_helloworld.database_obj;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PathDao {

    @Query("SELECT * FROM paths")
    public List<Path> getAll();

    @Query("SELECT * FROM paths WHERE name = :name")
    public Path findByName(String name);

    @Insert
    public Integer insertPath(Path p);

    @Delete
    public void deletePath(Path p);

    @Query("DELETE FROM paths")
    public void deleteAll();

}
