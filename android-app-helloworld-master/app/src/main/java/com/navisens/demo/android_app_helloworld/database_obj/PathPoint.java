package com.navisens.demo.android_app_helloworld.database_obj;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity (tableName = "points", foreignKeys = {@ForeignKey(onDelete = ForeignKey.CASCADE, entity = Path.class, parentColumns = "path_id",
childColumns = "path_id")},
        indices = {
        @Index("path_id")
})
public class PathPoint {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "path_point_id")
    @NonNull public long ppid;

    @ColumnInfo(name = "path_id")
    @NonNull public long pid;

    @ColumnInfo(name = "latitude")
    @NonNull public double latitude;

    @ColumnInfo(name = "longitude")
    @NonNull public double longitude;

    @ColumnInfo(name = "instruction")
    public String instruction;

    @ColumnInfo(name = "landmark")
    public String landmark;

    public PathPoint(PathPoint other) {
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.instruction = other.instruction;
        this.landmark = other.instruction;
        this.pid = other.pid;
        this.ppid = other.ppid;
    }

    public PathPoint() {

    }

    public PathPoint(double latitude, double longitude, long pathId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.pid = pathId;
    }

    public PathPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
