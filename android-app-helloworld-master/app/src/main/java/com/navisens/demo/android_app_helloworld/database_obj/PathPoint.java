package com.navisens.demo.android_app_helloworld.database_obj;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity (tableName = "points", foreignKeys = {@ForeignKey(entity = Path.class, parentColumns = "path_id",
childColumns = "path_point_id")})
public class PathPoint {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "path_point_id")
    public int ppid;

    @ColumnInfo(name = "path_id")
    public int pid;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "instruction")
    public String instruction;

    @ColumnInfo(name = "landmark")
    public String landmark;
}
