package com.android.app.navibuzz.database_obj;

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

    @Query("SELECT * FROM paths WHERE path_id = :pid")
    public Path getById(long pid);

    @Insert
    public Long insertPath(Path p);

    @Delete
    public void deletePath(Path p);

    @Query("DELETE FROM paths")
    public void deleteAll();

    @Query("UPDATE paths SET name = :new_name WHERE path_id = :pid")
    public void updateName(long pid, String new_name);

}
