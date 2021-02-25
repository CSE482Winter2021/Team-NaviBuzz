package com.navisens.demo.android_app_helloworld.database_obj;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"name"},
        unique = true)}, tableName = "paths")
public class Path {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "path_id")
    public int pid;

    @ColumnInfo(name = "name")
    public String name;
}
