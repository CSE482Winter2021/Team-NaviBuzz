package com.navisens.demo.android_app_helloworld.database_obj;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PathDao {

    @Query("SELECT * FROM paths")
    public LiveData<List<Path>> getAll();

    @Query("SELECT * FROM paths WHERE name = :name")
    public LiveData<Path> findByName(String name);

    @Query("SELECT * FROM paths WHERE path_id = :pid")
    public LiveData<Path> getById(long pid);

    @Insert
    public LiveData<Long> insertPath(Path p);

    @Delete
    public void deletePath(Path p);

    @Query("DELETE FROM paths")
    public void deleteAll();

}
